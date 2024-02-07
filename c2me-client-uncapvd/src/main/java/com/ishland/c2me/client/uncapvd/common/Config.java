package com.ishland.c2me.client.uncapvd.common;

import com.ishland.c2me.base.common.config.ConfigSystem;
import io.netty.util.internal.PlatformDependent;

public class Config {

    public static final int maxViewDistance = (int) new ConfigSystem.ConfigAccessor()
            .key("clientSideConfig.modifyMaxVDConfig.maxViewDistance")
            .comment("Max render distance allowed in game options")
            .getLong(getDefaultMaxVD(), getDefaultMaxVD(), ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY);

    private static int getDefaultMaxVD() {
        final double memoryInGiB = memoryInGiB();
        if (memoryInGiB < 1.0) return 16;
        if (memoryInGiB < 3.0) return 32;
        for (int i = 33; i <= 248; i ++) {
            if (memoryInMiBNeededForVD(i) / 1024.0 > memoryInGiB - 1) return i - 1;
        }
        return 248;
    }

    private static double memoryInMiBNeededForVD(int vd) {
        return Math.pow(vd * 2 + 1, 2) * (PlatformDependent.isJ9Jvm() ? 0.2 : 0.4);
    }

    private static double memoryInGiB() {
        return Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0 / 1024.0;
    }

    public static void init() {
    }

}
