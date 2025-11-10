package com.ishland.c2me.notickvd.common;

import com.ishland.c2me.base.common.C2MEConstants;
import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.base.common.config.ModStatuses;

public class Config {

    public static final int maxConcurrentChunkLoads = (int) new ConfigSystem.ConfigAccessor()
            .key("noTickViewDistance.maxConcurrentChunkLoads")
            .comment("No-tick view distance max concurrent chunk loads \n" +
                    " Lower this for a better latency and higher this for a faster loading")
            .getLong(GlobalExecutors.GLOBAL_EXECUTOR_PARALLELISM * 2L, GlobalExecutors.GLOBAL_EXECUTOR_PARALLELISM * 2L, ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY);

    public static final boolean enableExtRenderDistanceProtocol = new ConfigSystem.ConfigAccessor()
            .key("noTickViewDistance.enableExtRenderDistanceProtocol")
            .comment("""
                    Enable server-side support for extended render distance protocol (c2me:%s)
                    This allows requesting render distances higher than 127 chunks from the server
                    
                    Requires Fabric API (currently %s)
                    """.formatted(C2MEConstants.EXT_RENDER_DISTANCE_ID, ModStatuses.fabric_networking_api_v1 ? "available" : "unavailable"))
            .getBoolean(true, false);

    public static final boolean smoothChunkSendingRate = new ConfigSystem.ConfigAccessor()
            .key("noTickViewDistance.smoothChunkSendingRate")
            .comment("""
                    Whether to attempt to smooth out chunk sending rate
                    
                    Due to the nature of chunk loading and generation, chunks reach full status in bursts,
                    which can cause frame time stability if the server also delivers chunks in a bursty way
                    This config attempts to smooth out the bursty stream of chunks to help frame time stability
                    """)
            .getBoolean(true, false);

    public static final boolean ensureChunkCorrectness = new ConfigSystem.ConfigAccessor()
            .key("noTickViewDistance.ensureChunkCorrectness")
            .comment("Whether to ensure correct chunks within normal render distance \n" +
                    " This will send chunks twice increasing network load")
            .getBoolean(false, true);

    public static final int maxViewDistance = 1 << 16;

    public static void init() {
    }

}
