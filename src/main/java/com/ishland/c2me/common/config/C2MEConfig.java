package com.ishland.c2me.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.base.Preconditions;
import io.netty.util.internal.PlatformDependent;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class C2MEConfig {

    static final Logger LOGGER = LogManager.getLogger("C2ME Config");

    public static final AsyncIoConfig asyncIoConfig;
    public static final ThreadedWorldGenConfig threadedWorldGenConfig;
    public static final VanillaWorldGenOptimizationsConfig vanillaWorldGenOptimizationsConfig;
    public static final GeneralOptimizationsConfig generalOptimizationsConfig;
    public static final NoTickViewDistanceConfig noTickViewDistanceConfig;
    public static final ClientSideConfig clientSideConfig;

    static {
        long startTime = System.nanoTime();
        CommentedFileConfig config = CommentedFileConfig.builder(FabricLoader.getInstance().getConfigDir().resolve("c2me.toml"))
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();
        config.load();

        final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
        asyncIoConfig = new AsyncIoConfig(ConfigUtils.getValue(configScope, "asyncIO", ConfigUtils::config, "Configuration for async io system", List.of(), null));
        threadedWorldGenConfig = new ThreadedWorldGenConfig(ConfigUtils.getValue(configScope, "threadedWorldGen", ConfigUtils::config, "Configuration for threaded world generation", List.of(), null));
        vanillaWorldGenOptimizationsConfig = new VanillaWorldGenOptimizationsConfig(ConfigUtils.getValue(configScope, "vanillaWorldGenOptimizations", ConfigUtils::config, "Configuration for vanilla worldgen optimizations", List.of(), null));
        generalOptimizationsConfig = new GeneralOptimizationsConfig(ConfigUtils.getValue(configScope, "generalOptimizations", ConfigUtils::config, "Configuration for general optimizations", List.of(), null));
        noTickViewDistanceConfig = new NoTickViewDistanceConfig(ConfigUtils.getValue(configScope, "noTickViewDistance", ConfigUtils::config, "Configuration for no-tick view distance", List.of(), null));
        clientSideConfig = new ClientSideConfig(ConfigUtils.getValue(configScope, "clientSide", ConfigUtils::config, "Configuration for clientside functions", List.of(), null));
        configScope.removeUnusedKeys();
        config.save();
        config.close();
        LOGGER.info("Configuration loaded successfully after {}ms", (System.nanoTime() - startTime) / 1_000_000.0);
    }

    public static class AsyncIoConfig {
        public final boolean enabled;
        public final int serializerParallelism;

        public AsyncIoConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "asyncIo config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.enabled = ConfigUtils.getValue(configScope, "enabled", () -> true, "Whether to enable this feature", List.of("radon"), false);
            this.serializerParallelism = ConfigUtils.getValue(configScope, "serializerParallelism", () -> Math.max(2, Runtime.getRuntime().availableProcessors() / 4 - 1), "IO worker executor parallelism", List.of(), null, ConfigUtils.CheckType.THREAD_COUNT);
            configScope.removeUnusedKeys();
        }
    }

    public static class ThreadedWorldGenConfig {

        // For Testing Purposes
        private static final boolean global_enabled = Boolean.parseBoolean(System.getProperty("com.ishland.c2me.common.config.threadedWorldGen.enabled", "false"));
        private static final boolean global_allowThreadedFeatures = Boolean.parseBoolean(System.getProperty("com.ishland.c2me.common.config.threadedWorldGen.allowThreadedFeatures", "false"));
        private static final boolean global_useGlobalBiomeCache = Boolean.parseBoolean(System.getProperty("com.ishland.c2me.common.config.threadedWorldGen.useGlobalBiomeCache", "false"));
        private static final boolean global_reduceLockRadius = Boolean.parseBoolean(System.getProperty("com.ishland.c2me.common.config.threadedWorldGen.reduceLockRadius", "false"));

        public final boolean enabled;
        public final int parallelism;
        public final boolean allowThreadedFeatures;
        public final boolean reduceLockRadius;
        public final boolean useGlobalBiomeCache;

        private static int getWorldGenDefaultParallelism0() {
            if (PlatformDependent.isWindows()) {
                return Runtime.getRuntime().availableProcessors() / 2 - 2;
            } else {
                return (int) (Runtime.getRuntime().availableProcessors() / 1.6) - 2;
            }
        }

        public ThreadedWorldGenConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "threadedWorldGen config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.enabled = ConfigUtils.getValue(configScope, "enabled", () -> getWorldGenDefaultParallelism0() >= 2 || global_enabled, "Whether to enable this feature", List.of(), false);
            this.parallelism = ConfigUtils.getValue(configScope, "parallelism", () -> Math.max(2, getWorldGenDefaultParallelism0()), "World generation worker executor parallelism", List.of(), null, ConfigUtils.CheckType.THREAD_COUNT);
            this.allowThreadedFeatures = ConfigUtils.getValue(configScope, "allowThreadedFeatures", () -> true || global_allowThreadedFeatures, "Whether to allow feature generation (world decorations like trees, ores and etc.) run in parallel \n (may cause incompatibility with other mods)", List.of(), null);
            this.reduceLockRadius = ConfigUtils.getValue(configScope, "reduceLockRadius", () -> false || global_reduceLockRadius, "Whether to allow reducing lock radius \n (may cause incompatibility with other mods)", List.of(), null);
            this.useGlobalBiomeCache = ConfigUtils.getValue(configScope, "useGlobalBiomeCache", () -> false || global_useGlobalBiomeCache, "(Experimental in 1.18 snapshots) \n Whether to enable global MultiBiomeCache to accelerate worldgen \n This increases memory allocation ", List.of(), false);
            configScope.removeUnusedKeys();
        }
    }

    public static class VanillaWorldGenOptimizationsConfig {
        public final boolean enabled;
        public final boolean useEndBiomeCache;

        public VanillaWorldGenOptimizationsConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "vanillaWorldGenOptimizationsConfig config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.enabled = ConfigUtils.getValue(configScope, "enabled", () -> true, "Whether to enable this feature \n (may cause incompatibility with other mods)", List.of(), false);
            this.useEndBiomeCache = ConfigUtils.getValue(configScope, "useEndBiomeCache", () -> true, "Whether to enable End Biome Cache to accelerate The End worldgen \n This is included in lithium-fabric \n (may cause incompatibility with other mods) ", List.of("lithium"), false);
            configScope.removeUnusedKeys();
        }
    }

    public static class GeneralOptimizationsConfig {
        public final boolean optimizeAsyncChunkRequest;
        public final int chunkStreamVersion;

        public GeneralOptimizationsConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "generalOptimizationsConfig config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.optimizeAsyncChunkRequest = ConfigUtils.getValue(configScope, "optimizeAsyncChunkRequest", () -> true, "Whether to let async chunk request no longer block server thread \n (may cause incompatibility with other mods) ", List.of(), false);
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
                    List.of(), -1
            );
            configScope.removeUnusedKeys();
        }
    }

    public static class NoTickViewDistanceConfig {
        public final boolean enabled;
        public final int updatesPerTick;

        public NoTickViewDistanceConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "noTickViewDistanceConfig config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.enabled = ConfigUtils.getValue(configScope, "enabled", () -> false, "Weather to enable no-tick view distance", List.of(), false);
            this.updatesPerTick = ConfigUtils.getValue(configScope, "updatesPerTick", () -> 6, "No-tick view distance updates per tick \n Lower this for a better latency and higher this for a faster loading", List.of(), 6, ConfigUtils.CheckType.POSITIVE_VALUE_ONLY);
            configScope.removeUnusedKeys();
        }
    }

    public static class ClientSideConfig {
        public final ModifyMaxVDConfig modifyMaxVDConfig;

        public ClientSideConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "clientSideConfig config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.modifyMaxVDConfig = new ModifyMaxVDConfig(ConfigUtils.getValue(configScope, "modifyMaxVDConfig", ConfigUtils::config, "Configuration for modifying clientside max view distance", List.of(), null));
            configScope.removeUnusedKeys();
        }

        public static class ModifyMaxVDConfig {
            public final boolean enabled;
            public final int maxViewDistance;

            public ModifyMaxVDConfig(CommentedConfig config) {
                Preconditions.checkNotNull(config, "clientSideConfig config is not present");
                final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
                this.enabled = ConfigUtils.getValue(configScope, "enabled", () -> true, "Weather to enable c2me clientside features", List.of("bobby"), false);
                this.maxViewDistance = ConfigUtils.getValue(configScope, "maxViewDistance", () -> 64, "Max render distance allowed in game options", List.of(), 64, ConfigUtils.CheckType.NO_TICK_VIEW_DISTANCE);
                configScope.removeUnusedKeys();
            }
        }
    }

}
