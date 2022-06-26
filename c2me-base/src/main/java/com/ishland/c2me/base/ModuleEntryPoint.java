package com.ishland.c2me.base;

import com.ishland.c2me.base.common.config.ConfigSystem;
import io.netty.util.internal.PlatformDependent;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class ModuleEntryPoint {

    private static final boolean enabled = true;

    public static final long globalExecutorParallelism = new ConfigSystem.ConfigAccessor()
            .key("globalExecutorParallelism")
            .comment("Configures the parallelism of global executor")
            .getLong(getDefaultGlobalExecutorParallelism(), getDefaultGlobalExecutorParallelism(), ConfigSystem.LongChecks.THREAD_COUNT);

    public static int getDefaultGlobalExecutorParallelism() {
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
            return (int) ((memoryInGiB() + (isClientSide() ? -1.2 : -0.6)) / 1.2) + defaultParallelismEnvTypeOffset();
        }
    }

    private static double memoryInGiB() {
        return Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0 / 1024.0;
    }


}
