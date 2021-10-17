package com.ishland.curseforge.modpackresolver.provider;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.ishland.curseforge.modpackresolver.Constants;
import com.ishland.curseforge.modpackresolver.meta.CurseMeta;
import com.ishland.curseforge.modpackresolver.meta.ModPackManifest;
import com.ishland.curseforge.modpackresolver.util.FilesUtil;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.discovery.ModCandidate;
import net.fabricmc.loader.util.UrlConversionException;
import net.fabricmc.loader.util.UrlUtil;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.internal.hash.Hashing;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ModPackProvider {

    private static final long CURRENT_PLUGIN_VERSION = 0L;

    public static void provide(Dependency dependency, Project project) throws IOException {
        final String projectId = dependency.getName();
        final String fileId = dependency.getVersion();
        assert fileId != null;
        final Path cacheDir = project.getRootProject().file(".gradle").toPath().resolve(Constants.CACHE_DIR);
        final Path modpackCacheDir = cacheDir.resolve(projectId).resolve(fileId);
        if (!verifyModPack(project, modpackCacheDir) || !loadModResolutionCache(project, modpackCacheDir)) {
            final Path statusJson = modpackCacheDir.resolve("status.json");
            final Path repository = modpackCacheDir.resolve("repository");
            final CachedResolutionResults cachedResolutionResults = provideImpl(project, projectId, fileId);
            defineModsFromCache(project, repository, cachedResolutionResults);
            try (final Writer writer = Files.newBufferedWriter(statusJson, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
                new Gson().toJson(cachedResolutionResults, writer);
            } catch (IOException e) {
                try {
                    Files.delete(statusJson);
                } catch (Throwable t) {
                    e.addSuppressed(t);
                }
                project.getLogger().warn("Unable to save cache to " + statusJson, e);
            }
        }
        project.getLogger().lifecycle("Adding override files to run configurations");
        final Path decompressDir = modpackCacheDir.resolve(Constants.MODPACK_DECOMPRESSED_DIR);
        final Path manifestPath = decompressDir.resolve("manifest.json");
        final ModPackManifest modPackManifest = MetadataResolver.parseManifest(manifestPath);
        final Path overridesDir = decompressDir.resolve(modPackManifest.getOverrides());
        String className = "net.fabricmc.loom.task.RunGameTask";
        final List<Task> runGameTasks = project.getTasks().stream().filter(task -> task.getClass().getName().startsWith(className)).toList();
        for (Task runGameTask : runGameTasks) {
            runGameTask.doFirst(__ -> {
                try {
                    project.getLogger().lifecycle("Setting up modpack {}@{} file overrides before running the configuration", modPackManifest.getName(), modPackManifest.getVersion());
                    final Field configField = Class.forName("net.fabricmc.loom.task.AbstractRunTask", true, runGameTask.getClass().getClassLoader()).getDeclaredField("config");
                    configField.setAccessible(true);
                    final Object config = configField.get(runGameTask);
                    final List<Path> sourcePaths = Files.walk(overridesDir).toList();
                    final Path target = project.getRootDir().toPath().resolve((String) Class.forName("net.fabricmc.loom.configuration.ide.RunConfig", true, runGameTask.getClass().getClassLoader()).getField("runDir").get(config));
                    for (Path sourcePath : sourcePaths) {
                        Path targetPath = target.resolve(overridesDir.relativize(sourcePath).toString());
                        if (Files.isDirectory(sourcePath)) {
                            if (!Files.isDirectory(targetPath)) Files.createDirectories(targetPath);
                        } else if (Files.isRegularFile(sourcePath)) {
                            if (!Files.isDirectory(targetPath.getParent())) Files.createDirectories(targetPath.getParent());
                            if (!Files.isRegularFile(targetPath)) Files.copy(sourcePath, targetPath, StandardCopyOption.COPY_ATTRIBUTES);
                        }
                    }
                } catch (Throwable t) {
                    project.getLogger().warn("Unable to copy overrides to run configuration dir", t);
                }
            });
        }

    }

    private static boolean verifyModPack(Project project, Path modpackCacheDir) {
        if (project.getGradle().getStartParameter().isRefreshDependencies()) {
            project.getLogger().lifecycle("Refresh dependencies is turned on, disabling cache");
            return false;
        }
        try {
            final String expectedHash = Files.readString(modpackCacheDir.resolve(Constants.MODPACK_DOWNLOAD_FILENAME + ".sha256"));
            final String actualHash = Hashing.sha256().hashBytes(Files.readAllBytes(modpackCacheDir.resolve(Constants.MODPACK_DOWNLOAD_FILENAME))).toString();
            if (!expectedHash.equals(actualHash)) {
                project.getLogger().info("Hash mismatch (expected {} but got {}), falling back to uncached query", expectedHash, actualHash);
                return false;
            }
            decompressModpack(modpackCacheDir.resolve(Constants.MODPACK_DOWNLOAD_FILENAME), modpackCacheDir.resolve(Constants.MODPACK_DECOMPRESSED_DIR));
            return true;
        } catch (Throwable t) {
            project.getLogger().info("An error occurred while checking modpack integrity, falling back to uncached query", t);
            return false;
        }
    }

    private static boolean loadModResolutionCache(Project project, Path modpackCacheDir) {
        if (project.getGradle().getStartParameter().isRefreshDependencies()) {
            project.getLogger().lifecycle("Refresh dependencies is turned on, disabling cache");
            return false;
        }
        final Path statusJson = modpackCacheDir.resolve("status.json");
        final Path repository = modpackCacheDir.resolve("repository");
        if (Files.exists(statusJson)) {
            final CachedResolutionResults cachedResolutionResults;
            try (final Reader reader = Files.newBufferedReader(statusJson)) {
                cachedResolutionResults = new Gson().fromJson(reader, CachedResolutionResults.class);
            } catch (Throwable t) {
                project.getLogger().info("Cache failed, falling back to uncached query");
                return false;
            }
            if (cachedResolutionResults != null) {
                if (cachedResolutionResults.pluginVersion != CURRENT_PLUGIN_VERSION) {
                    project.getLogger().lifecycle("The previous cache is provided by a different plugin version (expected {} but got {}), falling back to uncached query", CURRENT_PLUGIN_VERSION, cachedResolutionResults.pluginVersion);
                    return false;
                }
                for (CachedResolutionResults.ModResolutionResult entry : cachedResolutionResults.modResolutionResults) {
                    try {
                        final Path jarPath = repository.resolve(String.format("%s-%s.jar", entry.modId, entry.modVersion));
                        final String jarHash = Hashing.sha256().hashBytes(Files.readAllBytes(jarPath)).toString();
                        if (!jarHash.equals(entry.sha256)) {
                            project.getLogger().lifecycle("Cache hash mismatch, falling back to uncached query");
                            return false;
                        }
                    } catch (Throwable t) {
                        project.getLogger().info("Some errors happened during hashing cached files, fallng back to uncached query", t);
                        return false;
                    }
                }
                project.getLogger().lifecycle("Defining {} mods from cache", cachedResolutionResults.modResolutionResults.size());
                defineModsFromCache(project, repository, cachedResolutionResults);
                return true;
            }
        }
        return false;
    }

    private static void defineModsFromCache(Project project, Path repository, CachedResolutionResults cachedResolutionResults) {
        project.getRepositories().flatDir(repo -> repo.dir(repository.toFile()));
        for (CachedResolutionResults.ModResolutionResult entry : cachedResolutionResults.modResolutionResults) {
            project.getDependencies().add("modImplementation", String.format("modpackresolver-generated:%s:%s", entry.modId, entry.modVersion));
        }
    }

    private static CachedResolutionResults provideImpl(Project project, String projectId, String fileId) throws IOException {
        final ModPackManifest modPackManifest = resolveModPack(projectId, fileId, project);
        final List<ModPackManifest.FilesItem> files = modPackManifest.getFiles();
        final Map<String, ModCandidate> resolvedMods = resolveModsAndNestedJars(project, projectId, fileId, modPackManifest, files);
        List<CachedResolutionResults.ModResolutionResult> resolutionResults = new ArrayList<>();
        defineMods(project, projectId, fileId, files, resolvedMods, resolutionResults::add);
        return new CachedResolutionResults(resolutionResults);
    }

    private static void defineMods(Project project, String projectId, String fileId, List<ModPackManifest.FilesItem> files, Map<String, ModCandidate> resolvedMods, Consumer<CachedResolutionResults.ModResolutionResult> dependencyConsumer) throws IOException {
        project.getLogger().lifecycle("Defining {} mods ({} normal, {} nested)", resolvedMods.size(), files.size(), resolvedMods.size() - files.size());
        final Path cacheDir = project.getRootProject().file(".gradle").toPath().resolve(Constants.CACHE_DIR);
        final Path repository = cacheDir.resolve(projectId).resolve(fileId).resolve("repository");
        repository.toFile().mkdirs();
        for (ModCandidate candidate : resolvedMods.values()) {
            final String modId = candidate.getInfo().getId();
            final Version version = candidate.getInfo().getVersion();
            String modVersion;

            if (version instanceof SemanticVersion) {
                modVersion = version.getFriendlyString();
            } else {
                try {
                    modVersion = "version-" + Hashing.md5().hashBytes(Files.readAllBytes(UrlUtil.asFile(candidate.getOriginUrl()).toPath()));
                } catch (Throwable t) {
                    project.getLogger().warn("Unable to hash file {}, using random UUID instead", candidate.getOriginUrl());
                    modVersion = "version-" + UUID.randomUUID();
                }
            }

            final Path jarPath = repository.resolve(String.format("%s-%s.jar", modId, modVersion));

            try {
                Files.copy(
                        UrlUtil.asFile(candidate.getOriginUrl()).toPath(),
                        jarPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
                stripMetaInf(jarPath);
                dependencyConsumer.accept(new CachedResolutionResults.ModResolutionResult(modId, modVersion, Hashing.sha256().hashBytes(Files.readAllBytes(jarPath)).toString()));
            } catch (UrlConversionException e) {
                throw new IOException(e);
            } catch (IOException e) {
                try {
                    Files.delete(jarPath);
                } catch (Throwable t) {
                    e.addSuppressed(t);
                }
                throw e;
            }
        }
    }

    private static void stripMetaInf(Path jarPath) throws IOException {
        try (final FileSystem fileSystem = FileSystems.newFileSystem(jarPath)) {
            for (Path rootDirectory : fileSystem.getRootDirectories()) {
                for (Path path : Files.walk(rootDirectory).sorted(Comparator.reverseOrder()).toList()) {
                    if (path.startsWith("/META-INF") && !path.toString().startsWith("/META-INF/services") && !path.getFileName().toString().endsWith(".accesswidener")) {
                        if (Files.isDirectory(path)) continue;
                        Files.delete(path);
                    }
                }
            }

        }
    }

    private static Map<String, ModCandidate> resolveModsAndNestedJars(Project project, String projectId, String fileId, ModPackManifest modPackManifest, List<ModPackManifest.FilesItem> files) throws IOException {
        project.getLogger().lifecycle("Resolving {} mods and their nested jars", files.size());
        List<Dependency> modDeps = new ArrayList<>(files.size());
        for (ModPackManifest.FilesItem file : files) {
            final DependencyHandler dependencies = project.getDependencies();
            final Dependency dep = getModDependency(file, dependencies);
            if (dep instanceof ModuleDependency moduleDependency)
                moduleDependency.setTransitive(false);
            modDeps.add(dep);
        }
        final Set<File> resolvedFiles = new HashSet<>(project.getConfigurations().detachedConfiguration(modDeps.toArray(Dependency[]::new)).resolve());
        final Path cacheDir = project.getRootProject().file(".gradle").toPath().resolve(Constants.CACHE_DIR);
        final Path modpackCacheDir = cacheDir.resolve(projectId).resolve(fileId);
        FabricModResolver.setGameDir(modpackCacheDir.resolve("download").resolve(modPackManifest.getOverrides()));
        final FabricModResolver fabricModResolver = new FabricModResolver(modpackCacheDir, project);
        for (File resolvedFile : resolvedFiles) {
            fabricModResolver.addMod(resolvedFile.toPath());
        }
        return fabricModResolver.getResolvedMods();
    }

    @NotNull
    private static Dependency getModDependency(ModPackManifest.FilesItem file, DependencyHandler dependencies) {
        if (file.getProjectID() == 413596 && file.getFileID() == 3421221) { // 0.11.0-pre
            // this fixes BetterEnd breaking yarn dev environment
            return dependencies.create("com.github.ishland:BetterEnd:7a3338b6");
        }
        if (file.getProjectID() == 413596 && file.getFileID() == 3390721) { // 0.10.5-pre
            // this fixes BetterEnd breaking yarn dev environment
            return dependencies.create("com.github.ishland:BetterEnd:e7dd0e0");
        }
        return dependencies.create(String.format("curse.maven:modpack-resolver-generated-%s:%s", file.getProjectID(), file.getFileID()));
    }

    private static ModPackManifest resolveModPack(String projectId, String fileId, Project project) throws IOException {
        final CurseMeta curseMeta = MetadataResolver.getCurseMeta(projectId, fileId, project);
        if (!curseMeta.getProject().getPackageType().equals("ModPack")) {
            throw new IllegalArgumentException("Not a modpack: " + curseMeta);
        }
        project.getLogger().lifecycle("Downloading ModPack {}", curseMeta.getDisplayName());
        final Path cacheDir = project.getRootProject().file(".gradle").toPath().resolve(Constants.CACHE_DIR);
        final Path modpackCacheDir = cacheDir.resolve(projectId).resolve(fileId);
        modpackCacheDir.toFile().mkdirs();
        final Path modpackFile = modpackCacheDir.resolve(Constants.MODPACK_DOWNLOAD_FILENAME);
        downloadUrl(curseMeta, modpackFile);
        project.getLogger().lifecycle("Decompressing and resolving ModPack manifest");
        final Path decompressDir = modpackCacheDir.resolve(Constants.MODPACK_DECOMPRESSED_DIR);
        decompressModpack(modpackFile, decompressDir);
        final Path manifestPath = decompressDir.resolve("manifest.json");
        return MetadataResolver.parseManifest(manifestPath);
    }

    private static void downloadUrl(CurseMeta curseMeta, Path modpackFile) throws IOException {
        final URL url = new URL(curseMeta.getDownloadURL());
        try (final InputStream in = url.openStream()) {
            Files.copy(in, modpackFile, StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(
                    modpackFile.getParent().resolve(modpackFile.getFileName() + ".sha256"),
                    Hashing.sha256().hashBytes(Files.readAllBytes(modpackFile)).toString()
            );
        } catch (IOException e) {
            // The file is probably broken, delete this
            try {
                Files.delete(modpackFile);
            } catch (IOException e1) {
                e.addSuppressed(e1);
            }
            throw e;
        }
    }

    private static void decompressModpack(Path modpackFile, Path decompressDir) throws IOException {
        decompressDir.toFile().mkdirs();
        FilesUtil.deleteDir(decompressDir);
        decompressDir.toFile().mkdirs();
        try {
            try (final ZipFile zipFile = new ZipFile(modpackFile.toFile())) {
                final Iterator<? extends ZipEntry> iterator = zipFile.entries().asIterator();
                while (iterator.hasNext()) {
                    final ZipEntry entry = iterator.next();
                    if (entry.getName().endsWith("/")) {
                        decompressDir.resolve(entry.getName()).toFile().mkdirs();
                    } else {
                        final Path file = decompressDir.resolve(entry.getName());
                        file.getParent().toFile().mkdirs();
                        try (final InputStream inputStream = zipFile.getInputStream(entry)) {
                            Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // The file is probably broken, delete this
            try {
                FilesUtil.deleteDir(decompressDir);
            } catch (IOException e1) {
                e.addSuppressed(e1);
            }
            throw e;
        }
    }

    private static class CachedResolutionResults {

        @SerializedName("pluginVersion")
        private long pluginVersion;

        @SerializedName("modResolutionResults")
        private List<ModResolutionResult> modResolutionResults;

        public CachedResolutionResults(List<ModResolutionResult> modResolutionResults) {
            this.pluginVersion = CURRENT_PLUGIN_VERSION;
            this.modResolutionResults = modResolutionResults;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CachedResolutionResults that = (CachedResolutionResults) o;
            return pluginVersion == that.pluginVersion && modResolutionResults.equals(that.modResolutionResults);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pluginVersion, modResolutionResults);
        }

        @Override
        public String toString() {
            return "CachedResolutionResults{" +
                    "pluginVersion=" + pluginVersion +
                    ", modResolutionResults=" + modResolutionResults +
                    '}';
        }

        private static class ModResolutionResult {

            @SerializedName("modId")
            private String modId;

            @SerializedName("modVersion")
            private String modVersion;

            @SerializedName("sha256Hash")
            private String sha256;

            public ModResolutionResult(String modId, String modVersion, String sha256) {
                this.modId = modId;
                this.modVersion = modVersion;
                this.sha256 = sha256;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ModResolutionResult that = (ModResolutionResult) o;
                return modId.equals(that.modId) && modVersion.equals(that.modVersion) && sha256.equals(that.sha256);
            }

            @Override
            public int hashCode() {
                return Objects.hash(modId, modVersion, sha256);
            }

            @Override
            public String toString() {
                return "ModResolutionResult{" +
                        "modId='" + modId + '\'' +
                        ", modVersion='" + modVersion + '\'' +
                        ", sha256='" + sha256 + '\'' +
                        '}';
            }
        }

    }

}
