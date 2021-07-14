package com.ishland.c2me.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.base.Preconditions;
import com.ishland.c2me.C2MEMod;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class C2MEConfig {

    static final Logger LOGGER = LogManager.getLogger("C2ME Config");

    public static final AsyncIoConfig asyncIoConfig;
    public static final ThreadedWorldGenConfig threadedWorldGenConfig;

    static {
        long startTime = System.nanoTime();
        CommentedFileConfig config = CommentedFileConfig.builder(FabricLoader.getInstance().getConfigDir().resolve("c2me.toml"))
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();
        config.load();

        final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
        asyncIoConfig = new AsyncIoConfig(ConfigUtils.getValue(configScope, "asyncIO", CommentedConfig::inMemory, "Configuration for async io system", List.of(), null));
        threadedWorldGenConfig = new ThreadedWorldGenConfig(ConfigUtils.getValue(configScope, "threadedWorldGen", CommentedConfig::inMemory, "Configuration for threaded world generation", List.of(), null));
        configScope.removeUnusedKeys();
        config.save();
        config.close();
        C2MEMod.LOGGER.info("Configuration loaded successfully after {}ms", (System.nanoTime() - startTime) / 1_000_000.0);
    }

    public static class AsyncIoConfig {
        public final boolean enabled;
        public final int serializerParallelism;

        public AsyncIoConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "asyncIo config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.enabled = ConfigUtils.getValue(configScope, "enabled", () -> true, "Whether to enable this feature", List.of("radon"), false);
            this.serializerParallelism = ConfigUtils.getValue(configScope, "serializerParallelism", () -> Math.max(2, Runtime.getRuntime().availableProcessors() / 6), "IO worker executor parallelism", List.of(), null, ConfigUtils.CheckType.THREAD_COUNT);
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

        public ThreadedWorldGenConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "threadedWorldGen config is not present");
            final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
            this.enabled = ConfigUtils.getValue(configScope, "enabled", () -> Runtime.getRuntime().availableProcessors() >= 4 || global_enabled, "Whether to enable this feature", List.of(), false);
            this.parallelism = ConfigUtils.getValue(configScope, "parallelism", () -> Math.max(2, Runtime.getRuntime().availableProcessors() / 2 - 2), "World generation worker executor parallelism", List.of(), null, ConfigUtils.CheckType.THREAD_COUNT);
            this.allowThreadedFeatures = ConfigUtils.getValue(configScope, "allowThreadedFeatures", () -> false || global_allowThreadedFeatures, "Whether to allow feature generation (world decorations like trees, ores and etc.) run in parallel \n (may cause incompatibility with other mods)", List.of(), null);
            this.reduceLockRadius = ConfigUtils.getValue(configScope, "reduceLockRadius", () -> false || global_reduceLockRadius, "Whether to allow reducing lock radius (faster but UNSAFE) (YOU HAVE BEEN WARNED) \n (may cause incompatibility with other mods)", List.of(), null);
            this.useGlobalBiomeCache = ConfigUtils.getValue(configScope, "useGlobalBiomeCache", () -> false || global_useGlobalBiomeCache, "(Experimental) Whether to enable global BiomeCache to accelerate worldgen \n This increases memory allocation ", List.of(), false);
            configScope.removeUnusedKeys();
        }
    }

}
