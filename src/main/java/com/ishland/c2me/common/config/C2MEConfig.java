package com.ishland.c2me.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.base.Preconditions;
import com.ishland.c2me.common.config.updater.Updaters;
import io.netty.util.internal.PlatformDependent;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

public class C2MEConfig {

    static final Logger LOGGER = LoggerFactory.getLogger("C2ME Config");

    private static final long CURRENT_CONFIG_VERSION = 2;

    public static final IoSystemConfig ioSystemConfig;
    public static final ThreadedWorldGenConfig threadedWorldGenConfig;
    public static final AsyncSchedulingConfig asyncSchedulingConfig;
    public static final VanillaWorldGenOptimizationsConfig vanillaWorldGenOptimizationsConfig;
    public static final GeneralOptimizationsConfig generalOptimizationsConfig;
    public static final NoTickViewDistanceConfig noTickViewDistanceConfig;
    public static final ClientSideConfig clientSideConfig;
    public static final int globalExecutorParallelism;

    static {
        long startTime = System.nanoTime();
        final Supplier<CommentedFileConfig> configSupplier = () -> CommentedFileConfig.builder(FabricLoader.getInstance().getConfigDir().resolve("c2me.toml"))
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();
        CommentedFileConfig config;
        try {
            config = configSupplier.get();
            config.load();
        } catch (Throwable t) {
            t.printStackTrace();
            config = configSupplier.get();
            config.save();
        }

        final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
        configScope.processedKeys.add("version");
        Updaters.update(config);
        final long version = config.getLongOrElse("version", 0);
        Preconditions.checkState(CURRENT_CONFIG_VERSION == version, "Config version mismatch");
        globalExecutorParallelism = ConfigUtils.getValue(configScope, "globalExecutorParallelism", C2MEConfig::getDefaultGlobalExecutorParallelism, "Configures the parallelism of global executor", List.of(), null, true);
        ioSystemConfig = new IoSystemConfig(ConfigUtils.getValue(configScope, "ioSystem", ConfigUtils::config, "Configuration for io system", List.of(), null, true));
        threadedWorldGenConfig = new ThreadedWorldGenConfig(ConfigUtils.getValue(configScope, "threadedWorldGen", ConfigUtils::config, "Configuration for threaded world generation", List.of(), null, true));
        asyncSchedulingConfig = new AsyncSchedulingConfig(ConfigUtils.getValue(configScope, "asyncScheduling", ConfigUtils::config, "Configuration for async scheduling system", List.of(), null, true));
        vanillaWorldGenOptimizationsConfig = new VanillaWorldGenOptimizationsConfig(ConfigUtils.getValue(configScope, "vanillaWorldGenOptimizations", ConfigUtils::config, "Configuration for vanilla worldgen optimizations", List.of(), null, true));
        generalOptimizationsConfig = new GeneralOptimizationsConfig(ConfigUtils.getValue(configScope, "generalOptimizations", ConfigUtils::config, "Configuration for general optimizations", List.of(), null, true));
        noTickViewDistanceConfig = new NoTickViewDistanceConfig(ConfigUtils.getValue(configScope, "noTickViewDistance", ConfigUtils::config, "Configuration for no-tick view distance", List.of(), null, true));
        clientSideConfig = new ClientSideConfig(ConfigUtils.getValue(configScope, "clientSide", ConfigUtils::config, "Configuration for clientside functions", List.of(), null, true));
        configScope.removeUnusedKeys();
        config.save();
        config.close();
        LOGGER.info("Configuration loaded successfully after {}ms", (System.nanoTime() - startTime) / 1_000_000.0);
    }

    private static int getDefaultGlobalExecutorParallelism() {
        return Math.max(1, Math.min(getDefaultParallelismCPU(), getDefaultParallelismHeap()));
    }

    private static int getDefaultParallelismCPU() {
        if (PlatformDependent.isWindows()) {
            return Math.max(1, (int) (Runtime.getRuntime().availableProcessors() / 1.6 - 2)) + defaultParallelismEnvTypeOffset();
        } else {
            return Math.max(1, (int) (Runtime.getRuntime().availableProcessors() / 1.2 - 2)) + defaultParallelismEnvTypeOffset();
        }
    }

    private static int defaultParallelismEnvTypeOffset() {
        return isClientSide() ? -2 : 0;
    }

    private static boolean isClientSide() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    private static int getDefaultParallelismHeap() {
        if (PlatformDependent.isJ9Jvm()) {
            return (int) ((memoryInGiB() + (isClientSide() ? -0.6 : -0.2)) / 0.5) + defaultParallelismEnvTypeOffset();
        } else {
            return (int) ((memoryInGiB() + (isClientSide() ? -1.8 : -0.6)) / 1.4) + defaultParallelismEnvTypeOffset();
        }
    }

    private static double memoryInGiB() {
        return Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0 / 1024.0;
    }

    public static class IoSystemConfig {
        public final boolean async;
        public final boolean replaceImpl;
        public final int chunkDataCacheSoftLimit;
        public final int chunkDataCacheLimit;

        public IoSystemConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "ioSystem config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.async = ConfigUtils.getValue(configScope, "async", () -> true, "Whether to use async chunk loading & unloading", List.of("radon"), false, true);
            this.replaceImpl = ConfigUtils.getValue(configScope, "replaceImpl", () -> false, "(WIP) Whether to use optimized implementation of IO system", List.of("radon"), false, true);
            this.chunkDataCacheSoftLimit = ConfigUtils.getValue(configScope, "chunkDataCacheSoftLimit", () -> 1536, "Soft limit for io worker nbt cache", List.of(), 4096, true);
            this.chunkDataCacheLimit = ConfigUtils.getValue(configScope, "chunkDataCacheLimit", () -> 6144, "Hard limit for io worker nbt cache", List.of(), 8192, true);
            configScope.removeUnusedKeys();
        }
    }

    public static class ThreadedWorldGenConfig {

        // For Testing Purposes
        private static final boolean global_enabled = Boolean.parseBoolean(System.getProperty("com.ishland.c2me.common.config.threadedWorldGen.enabled", "false"));
        private static final boolean global_useGlobalBiomeCache = Boolean.parseBoolean(System.getProperty("com.ishland.c2me.common.config.threadedWorldGen.useGlobalBiomeCache", "false"));
        private static final boolean global_reduceLockRadius = Boolean.parseBoolean(System.getProperty("com.ishland.c2me.common.config.threadedWorldGen.reduceLockRadius", "false"));

        public final boolean enabled;
        public final boolean allowThreadedFeatures;
        public final boolean reduceLockRadius;
        public final boolean useGlobalBiomeCache = false;

        public ThreadedWorldGenConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "threadedWorldGen config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.enabled = ConfigUtils.getValue(configScope, "enabled", () -> getDefaultGlobalExecutorParallelism() >= 3 || global_enabled, "Whether to enable this feature", List.of(), false, true);
            this.allowThreadedFeatures = ConfigUtils.getValue(configScope, "allowThreadedFeatures", () -> true, "Whether to allow feature generation (world decorations like trees, ores and etc.) run in parallel \n (may cause incompatibility with other mods)", List.of(), null, true);
            this.reduceLockRadius = ConfigUtils.getValue(configScope, "reduceLockRadius", () -> true, "Whether to allow reducing lock radius \n (may cause incompatibility with other mods)", List.of(), null, true);
//            this.useGlobalBiomeCache = ConfigUtils.getValue(configScope, "useGlobalBiomeCache", () -> false, "(DO NOT USE in 1.18) \n Whether to enable global BiomeCache to accelerate worldgen \n This increases memory allocation ", List.of(), false, true);
            configScope.removeUnusedKeys();
        }
    }

    public static class AsyncSchedulingConfig {
        private static final boolean global_enabled = Boolean.parseBoolean(System.getProperty("com.ishland.c2me.common.config.asyncScheduling.enabled", "false"));

        public final boolean enabled;

        public AsyncSchedulingConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "asyncSchedulingConfig config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.enabled = ConfigUtils.getValue(configScope, "enabled", () -> false || global_enabled, "(Experimental) Whether to enable this feature \n (may cause incompatibility with other mods)", List.of(), false, true);
            configScope.removeUnusedKeys();
        }
    }

    public static class VanillaWorldGenOptimizationsConfig {
        public final boolean enabled;
        public final boolean useEndBiomeCache;

        public VanillaWorldGenOptimizationsConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "vanillaWorldGenOptimizationsConfig config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.enabled = ConfigUtils.getValue(configScope, "enabled", () -> true, "Whether to enable this feature \n (may cause incompatibility with other mods)", List.of(), false, true);
            this.useEndBiomeCache = ConfigUtils.getValue(configScope, "useEndBiomeCache", () -> true, "Whether to enable End Biome Cache to accelerate The End worldgen \n This is included in lithium-fabric \n (may cause incompatibility with other mods) ", List.of("lithium"), false, true);
            configScope.removeUnusedKeys();
        }
    }

    public static class GeneralOptimizationsConfig {
        public final boolean optimizeAsyncChunkRequest;
        public final int chunkStreamVersion;
        public final boolean doMidTickChunkTasks;

        public GeneralOptimizationsConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "generalOptimizationsConfig config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.optimizeAsyncChunkRequest = ConfigUtils.getValue(configScope, "optimizeAsyncChunkRequest", () -> true, "Whether to let async chunk request no longer block server thread \n (may cause incompatibility with other mods) ", List.of(), false, true);
            this.chunkStreamVersion = ConfigUtils.getValue(configScope, "chunkStreamVersion", () -> -1,
                    """
                            Defines which chunk compression should be used\s
                             -1 for Vanilla default (also prevents mixin modifying vanilla default \s
                             from being applied)\s
                             1  for GZip (RFC1952) (Vanilla compatible)\s
                             2  for Zlib (RFC1950) (Vanilla default) (Vanilla compatible)\s
                             3  for Uncompressed (Fastest, but higher disk usage) (Vanilla compatible)\s
                             \s
                             Original chunk data will still readable after modifying this option \s
                             as this option only affects newly stored chunks\s
                             Other values can result in crashes when starting minecraft \s
                             to prevent further damage
                             """,
                    List.of(), -1,
                    true);
            this.doMidTickChunkTasks = ConfigUtils.getValue(configScope, "doMidTickChunkTasks", () -> true,
                    """
                            Whether to enable mid-tick chunk tasks \s
                             Mid-tick chunk tasks is to execute chunk tasks during server tick loop \s
                             to speed up chunk loading and generation \s
                             This helps chunks loading and generating under high MSPT but may raise \s
                             MSPT when chunks are loading or generating \s
                             \s
                             Incompatible with Dimensional Threading (dimthread)
                            """,
                    List.of("dimthread"), false, true);
            configScope.removeUnusedKeys();
        }
    }

    public static class NoTickViewDistanceConfig {
        private static final boolean global_enabled = Boolean.parseBoolean(System.getProperty("com.ishland.c2me.common.config.noTickViewDistance.enabled", "false"));

        public final boolean enabled;
        public final int updatesPerTick;
        public final boolean compatibilityMode;
        public final boolean ensureChunkCorrectness;

        public NoTickViewDistanceConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "noTickViewDistanceConfig config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.enabled = ConfigUtils.getValue(configScope, "enabled", () -> true, "Weather to enable no-tick view distance", List.of(), false, true);
            this.updatesPerTick = ConfigUtils.getValue(configScope, "updatesPerTick", () -> 6, "No-tick view distance updates per tick \n Lower this for a better latency and higher this for a faster loading", List.of(), 6, true);
            this.compatibilityMode = ConfigUtils.getValue(configScope, "compatibilityMode", () -> false, "Whether to use compatibility mode to send chunks \n This may fix some mod compatibility issues", List.of("antixray"), true, true);
            this.ensureChunkCorrectness = ConfigUtils.getValue(configScope, "ensureChunkCorrectness", () -> false, "Whether to ensure correct chunks within normal render distance \n This will send chunks twice increasing network load", List.of(), false, true);
            configScope.removeUnusedKeys();
        }
    }

    public static class ClientSideConfig {
        public final ModifyMaxVDConfig modifyMaxVDConfig;

        public ClientSideConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "clientSideConfig config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.modifyMaxVDConfig = new ModifyMaxVDConfig(ConfigUtils.getValue(configScope, "modifyMaxVDConfig", ConfigUtils::config, "Configuration for modifying clientside max view distance", List.of(), null, true));
            configScope.removeUnusedKeys();
        }

        public static class ModifyMaxVDConfig {
            public final boolean enabled;
            public final int maxViewDistance;

            public ModifyMaxVDConfig(CommentedConfig config) {
                Preconditions.checkNotNull(config, "clientSideConfig config is not present");
                final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
                this.enabled = ConfigUtils.getValue(configScope, "enabled", () -> true, "Weather to enable c2me clientside features", List.of("bobby"), false, true);
                this.maxViewDistance = ConfigUtils.getValue(configScope, "maxViewDistance", () -> 128, "Max render distance allowed in game options", List.of(), 128, true);
                configScope.removeUnusedKeys();
            }
        }
    }

}
