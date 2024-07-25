package com.ishland.c2me.rewrites.chunksystem.common;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class Config {

    public static final boolean asyncSerialization = new ConfigSystem.ConfigAccessor()
            .key("chunkSystem.asyncSerialization")
            .comment("""
                    Whether to enable async serialization
                    """)
            .getBoolean(true, false);

    public static void init() {
        // intentionally empty
    }

}
