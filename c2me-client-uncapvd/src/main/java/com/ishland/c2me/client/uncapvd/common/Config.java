package com.ishland.c2me.client.uncapvd.common;

import com.ishland.c2me.base.common.C2MEConstants;
import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.base.common.config.ModStatuses;
import io.netty.util.internal.PlatformDependent;

public class Config {

    public static final int maxViewDistance = (int) new ConfigSystem.ConfigAccessor()
            .key("clientSideConfig.modifyMaxVDConfig.maxViewDistance")
            .comment("Max render distance allowed in game options")
            .getLong(getDefaultMaxVD(), getDefaultMaxVD(), ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY);

    public static final boolean enableExtRenderDistanceProtocol = new ConfigSystem.ConfigAccessor()
            .key("clientSideConfig.modifyMaxVDConfig.enableExtRenderDistanceProtocol")
            .comment("""
                    Enable client-side support for extended render distance protocol (c2me:%s)
                    This allows requesting render distances higher than 127 chunks from the server
                    
                    Requires Fabric API (currently %s)
                    
                    Note: The server must advertise support this protocol for this to work
                    """.formatted(C2MEConstants.EXT_RENDER_DISTANCE_ID, ModStatuses.fabric_networking_api_v1 ? "available" : "unavailable"))
            .getBoolean(true, false);

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
