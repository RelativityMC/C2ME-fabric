package com.ishland.curseforge.modpackresolver.provider;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.discovery.ModCandidate;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.loader.lib.gson.MalformedJsonException;
import net.fabricmc.loader.metadata.LoaderModMetadata;
import net.fabricmc.loader.metadata.ModMetadataParser;
import net.fabricmc.loader.metadata.NestedJarEntry;
import net.fabricmc.loader.metadata.ParseMetadataException;
import net.fabricmc.loader.util.UrlConversionException;
import net.fabricmc.loader.util.UrlUtil;
import org.apache.logging.log4j.LogManager;
import org.gradle.api.Project;
import org.gradle.internal.hash.Hashing;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Simplified mod loading
public class FabricModResolver {

    private static final FabricLauncherBase FABRIC_LAUNCHER_BASE = new DummyFabricLauncherBase();

    public static void setGameDir(Path path) {
        try {
            final Method setGameDir = FabricLoader.class.getDeclaredMethod("setGameDir", Path.class);
            setGameDir.setAccessible(true);
            setGameDir.invoke(net.fabricmc.loader.api.FabricLoader.getInstance(), path);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to modify fabric-loader game path", t);
        }
    }

    private final Map<String, ModCandidate> candidatesById = new HashMap<>();
    private final Map<String, List<Path>> nestedJarCache = new HashMap<>();

    private final Path cacheDir;
    private final Project project;

    public FabricModResolver(Path cacheDir, Project project) {
        this.cacheDir = cacheDir;
        this.project = project;
        if (net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir() == null) throw new NullPointerException();
    }

    public void addMod(Path path) throws IOException {
        resolveMetadata(path, 0);
    }

    public Map<String, ModCandidate> getResolvedMods() {
        return Collections.unmodifiableMap(candidatesById);
    }

    private void resolveMetadata(Path path, int depth) throws IOException {
        final FileSystem fileSystem = FileSystems.newFileSystem(path);
        final Path modJson = fileSystem.getPath("fabric.mod.json");
        final Path rootDir = fileSystem.getRootDirectories().iterator().next();
        final URL normalizedUrl;
        try {
            normalizedUrl = UrlUtil.asUrl(path);
        } catch (UrlConversionException e) {
            throw new IOException(e);
        }
        final LoaderModMetadata info;
        try {
            info = ModMetadataParser.parseMetadata(LogManager.getLogger(), modJson);
        } catch (MalformedJsonException | ParseMetadataException e) {
            throw new RuntimeException(String.format("Mod at \"%s\" has an invalid fabric.mod.json file!", path), e);
        } catch (NoSuchFileException e) {
            project.getLogger().warn(String.format("Non-Fabric mod JAR at \"%s\", ignoring", path));
            return;
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Failed to parse mod metadata for mod at \"%s\"", path), t);
        }
        ModCandidate candidate = new ModCandidate(info, normalizedUrl, depth, false);

        if (!(candidate.getInfo().getVersion() instanceof SemanticVersion)) {
            project.getLogger().warn("{}@{} does not follow semantic version. Comparison support is limited. ", candidate.getInfo().getId(), candidate.getInfo().getVersion());
        }

        // duplicate check
        final String modId = candidate.getInfo().getId();
        if (candidatesById.containsKey(modId)) {
            final ModCandidate existingCandidate = candidatesById.get(modId);
            final Version version = candidate.getInfo().getVersion();
            final Version existingVersion = existingCandidate.getInfo().getVersion();
            boolean isOverwritten = false;
            if (existingCandidate.getDepth() > candidate.getDepth()) {
                project.getLogger().info("{}@{} -> {}@{}: lower nested depth", modId, existingVersion, modId, version);
                isOverwritten = true;
            }
            if (version instanceof SemanticVersion) {
                if (!(existingVersion instanceof SemanticVersion)) {
                    project.getLogger().info("{}@{} -> {}@{}: follows semantic version", modId, existingVersion, modId, version);
                    isOverwritten = true;
                } else {
                    if (((SemanticVersion) version).compareTo((SemanticVersion) existingVersion) > 0) {
                        project.getLogger().info("{}@{} -> {}@{}: newer", modId, existingVersion, modId, version);
                        isOverwritten = true;
                    }
                }
            }
            if (!isOverwritten) return;
        }

        candidatesById.put(modId, candidate);

        List<Path> nestedJars = nestedJarCache.computeIfAbsent(candidate.getOriginUrl().toString(), url -> {
            project.getLogger().debug("Searching for nested JARs in {}", url);
            Collection<NestedJarEntry> jars = candidate.getInfo().getJars();
            List<Path> list = new ArrayList<>(jars.size());

            for (NestedJarEntry jar : jars) {
                Path modPath = rootDir.resolve(jar.getFile().replace("/", rootDir.getFileSystem().getSeparator()));
                if (!Files.isDirectory(modPath) && modPath.toString().endsWith(".jar")) {
                    project.getLogger().debug("Found nested JAR: " + modPath);
                    Path dest;

                    try {
                        final String hash = Hashing.sha256().hashBytes(Files.readAllBytes(modPath)).toString();
                        dest = cacheDir.resolve("nested-jars").resolve(hash + ".jar");
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to calculate nested JAR " + modPath + " hash");
                    }

                    dest.getParent().toFile().mkdirs();

                    try {
                        Files.copy(modPath, dest, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        try {
                            Files.delete(dest);
                        } catch (Throwable t) {
                            e.addSuppressed(t);
                        }
                        throw new RuntimeException("Failed to load nested JAR " + modPath + " into cache (" + dest + ")!", e);
                    }

                    list.add(dest);
                }
            }

            return list;
        });

        for (Path nestedJar : nestedJars) {
            resolveMetadata(nestedJar, depth + 1);
        }
    }

    private static class DummyFabricLauncherBase extends FabricLauncherBase {

        @Override
        public void propose(URL url) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EnvType getEnvironmentType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isClassLoaded(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ClassLoader getTargetClassLoader() {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] getClassByteArray(String name, boolean runTransformers) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDevelopment() {
            return true;
        }

        @Override
        public String getEntrypoint() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getTargetNamespace() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<URL> getLoadTimeDependencies() {
            throw new UnsupportedOperationException();
        }
    }

}
