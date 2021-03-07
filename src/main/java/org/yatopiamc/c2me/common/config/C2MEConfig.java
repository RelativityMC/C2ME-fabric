package org.yatopiamc.c2me.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.base.Preconditions;
import net.fabricmc.loader.api.FabricLoader;

import java.net.URL;

public class C2MEConfig {

    public static final AsyncIoConfig asyncIoConfig;
    public static final ThreadedWorldGenConfig threadedWorldGenConfig;

    static {
        URL defaultConfigURL = C2MEConfig.class.getClassLoader().getResource("default-c2me.toml");
        Preconditions.checkNotNull(defaultConfigURL, "Default configuration file does not exist.");

        CommentedFileConfig config = CommentedFileConfig.builder(FabricLoader.getInstance().getConfigDir().resolve("c2me.toml"))
                .defaultData(defaultConfigURL)
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();
        config.load();

        asyncIoConfig = new AsyncIoConfig(config.get("asyncIO"));
        threadedWorldGenConfig = new ThreadedWorldGenConfig(config.get("threadedWorldGen"));
        config.save();
        config.close();
    }

    public static class AsyncIoConfig {
        public final int serializerParallelism;
        public final int ioWorkerParallelism;

        public AsyncIoConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "asyncIo config is not present");
            int configuredSerializerParallelism = config.getIntOrElse("serializerParallelism", -1);
            Preconditions.checkArgument(configuredSerializerParallelism >= -1 && configuredSerializerParallelism != 0 && configuredSerializerParallelism <= 0x7fff, "Invalid serializerParallelism");
            if (configuredSerializerParallelism == -1)
                serializerParallelism = Math.min(2, Runtime.getRuntime().availableProcessors());
            else
                serializerParallelism = configuredSerializerParallelism;
            int configuredIoWorkerParallelism = config.getIntOrElse("ioWorkerParallelism", -1);
            Preconditions.checkArgument(configuredIoWorkerParallelism >= -1 && configuredIoWorkerParallelism != 0 && configuredIoWorkerParallelism <= 0x7fff, "Invalid ioWorkerParallelism");
            if (configuredIoWorkerParallelism == -1)
                ioWorkerParallelism = Math.min(10, Runtime.getRuntime().availableProcessors());
            else
                ioWorkerParallelism = configuredIoWorkerParallelism;
        }
    }

    public static class ThreadedWorldGenConfig {
        public final boolean enabled;
        public final int parallelism;
        public final boolean allowThreadedFeatures;

        public ThreadedWorldGenConfig(CommentedConfig config) {
            Preconditions.checkNotNull(config, "threadedWorldGen config is not present");
            upgrade(config);
            enabled = config.getOrElse("enabled", false);
            int configuredParallelism = config.getIntOrElse("parallelism", -1);
            Preconditions.checkArgument(configuredParallelism >= -1 && configuredParallelism != 0 && configuredParallelism <= 0x7fff, "Invalid parallelism");
            if (configuredParallelism == -1)
                parallelism = Math.min(6, Runtime.getRuntime().availableProcessors());
            else
                parallelism = configuredParallelism;
            allowThreadedFeatures = config.getOrElse("allowThreadedFeatures", false);
        }

        private void upgrade(CommentedConfig config) {
            if (!config.contains("allowThreadedFeatures")) {
                config.set("allowThreadedFeatures", false);
                config.setComment("allowThreadedFeatures", " Whether to allow feature generation (world decorations like trees, ores and etc.) run in parallel");
            }
        }
    }

}
