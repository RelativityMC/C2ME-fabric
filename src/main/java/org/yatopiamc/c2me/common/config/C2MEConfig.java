package org.yatopiamc.c2me.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.base.Preconditions;
import net.fabricmc.loader.api.FabricLoader;
import org.yatopiamc.c2me.C2MEMod;

import java.net.URL;

public class C2MEConfig {

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

        asyncIoConfig = new AsyncIoConfig(ConfigUtils.getValue(config, "asyncIO", CommentedConfig::inMemory, "Configuration for async io system"));
        threadedWorldGenConfig = new ThreadedWorldGenConfig(ConfigUtils.getValue(config, "threadedWorldGen", CommentedConfig::inMemory, "Configuration for threaded world generation"));
        config.save();
        config.close();
        C2MEMod.LOGGER.info("Configuration loaded successfully after {}ms", (System.nanoTime() - startTime) / 1_000_000.0);
    }

    public static class AsyncIoConfig {
        public final int serializerParallelism;
        public final int ioWorkerParallelism;

        public AsyncIoConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "asyncIo config is not present");
            this.serializerParallelism = ConfigUtils.getValue(config, "serializerParallelism", () -> Math.min(2, Runtime.getRuntime().availableProcessors()), "IO worker executor parallelism", ConfigUtils.CheckType.THREAD_COUNT);
            this.ioWorkerParallelism = ConfigUtils.getValue(config, "ioWorkerParallelism", () -> Math.min(6, Runtime.getRuntime().availableProcessors()), "Serializer executor parallelism", ConfigUtils.CheckType.THREAD_COUNT);
        }
    }

    public static class ThreadedWorldGenConfig {
        public final boolean enabled;
        public final int parallelism;
        public final boolean allowThreadedFeatures;

        public ThreadedWorldGenConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "threadedWorldGen config is not present");
            this.enabled = ConfigUtils.getValue(config, "enabled", () -> true, "Whether to enable this feature");
            this.parallelism = ConfigUtils.getValue(config, "parallelism", () -> Math.min(6, Runtime.getRuntime().availableProcessors()), "World generation worker executor parallelism", ConfigUtils.CheckType.THREAD_COUNT);
            this.allowThreadedFeatures = ConfigUtils.getValue(config, "allowThreadedFeatures", () -> false, "Whether to allow feature generation (world decorations like trees, ores and etc.) run in parallel");
        }
    }

}
