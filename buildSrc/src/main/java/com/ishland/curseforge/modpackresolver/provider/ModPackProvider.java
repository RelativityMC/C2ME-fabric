package com.ishland.curseforge.modpackresolver.provider;

import com.ishland.curseforge.modpackresolver.Constants;
import com.ishland.curseforge.modpackresolver.meta.CurseMeta;
import com.ishland.curseforge.modpackresolver.meta.ModPackManifest;
import com.ishland.curseforge.modpackresolver.util.FilesUtil;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ModPackProvider {

    public static void provide(Dependency dependency, Project project) throws IOException {
        final String projectId = dependency.getName();
        final String fileId = dependency.getVersion();
        final List<ModPackManifest.FilesItem> files = resolveModPack(projectId, fileId, project).getFiles();
        project.getLogger().lifecycle("Defining {} mods", files.size());
        for (ModPackManifest.FilesItem file : files) {
            final DependencyHandler dependencies = project.getDependencies();
            final Dependency dep = getModDependency(projectId, fileId, file, dependencies);
            final Dependency modImplementation = dependencies.add("modImplementation", dep);
            if (modImplementation instanceof ModuleDependency moduleDependency) {
                moduleDependency.setTransitive(false);
            }
        }
    }

    @NotNull
    private static Dependency getModDependency(String projectId, String fileId, ModPackManifest.FilesItem file, DependencyHandler dependencies) {
        if (file.getProjectID() == 413596 && file.getFileID() == 3390721) {
            // this fixes BetterEnd breaking yarn dev environment
            return dependencies.create("com.github.ishland:BetterEnd:2cce1d2");
        }
        return dependencies.create(String.format("curse.maven:modpack-resolver-generated-%s-%s-%s:%s", projectId, fileId, file.getProjectID(), file.getFileID()));
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
            final ZipFile zipFile = new ZipFile(modpackFile.toFile());
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
