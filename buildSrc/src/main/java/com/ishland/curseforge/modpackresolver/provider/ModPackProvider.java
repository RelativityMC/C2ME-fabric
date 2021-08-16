package com.ishland.curseforge.modpackresolver.provider;

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
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.internal.hash.Hashing;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ModPackProvider {

    public static void provide(Dependency dependency, Project project) throws IOException {
        final String projectId = dependency.getName();
        final String fileId = dependency.getVersion();
        assert fileId != null;
        final ModPackManifest modPackManifest = resolveModPack(projectId, fileId, project);
        final List<ModPackManifest.FilesItem> files = modPackManifest.getFiles();
        final Map<String, ModCandidate> resolvedMods = resolveModsAndNestedJars(project, projectId, fileId, modPackManifest, files);
        defineMods(project, projectId, fileId, files, resolvedMods);

    }

    private static void defineMods(Project project, String projectId, String fileId, List<ModPackManifest.FilesItem> files, Map<String, ModCandidate> resolvedMods) throws IOException {
        project.getLogger().lifecycle("Defining {} mods ({} normal, {} nested)", resolvedMods.size(), files.size(), resolvedMods.size() - files.size());
        final Path cacheDir = project.getRootProject().file(".gradle").toPath().resolve(Constants.CACHE_DIR);
        final Path repository = cacheDir.resolve(projectId).resolve(fileId).resolve("repository");
        repository.toFile().mkdirs();
        project.getRepositories().flatDir(repo -> repo.dir(repository.toFile()));
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
                project.getDependencies().add("modImplementation", String.format("modpackresolver-generated:%s:%s", modId, modVersion));
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
                    if (path.startsWith("/META-INF")) {
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
        final Map<String, ModCandidate> resolvedMods = fabricModResolver.getResolvedMods();
        return resolvedMods;
    }

    @NotNull
    private static Dependency getModDependency(ModPackManifest.FilesItem file, DependencyHandler dependencies) {
        if (file.getProjectID() == 413596 && file.getFileID() == 3421221) { // 0.11.0-pre
            // this fixes BetterEnd breaking yarn dev environment
            return dependencies.create("com.github.ishland:BetterEnd:2cce1d2");
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

}
