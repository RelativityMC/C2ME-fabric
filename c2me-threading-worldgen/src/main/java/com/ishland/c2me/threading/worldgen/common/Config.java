package com.ishland.c2me.threading.worldgen.common;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class Config {

    public static final boolean allowThreadedFeatures = new ConfigSystem.ConfigAccessor()
            .key("threadedWorldGen.allowThreadedFeatures")
            .comment("Whether to allow feature generation (world decorations like trees, ores and etc.) run in parallel\n" +
                    "(may cause incompatibility with other mods)")
            .getBoolean(true, false);

    public static final boolean reduceLockRadius = new ConfigSystem.ConfigAccessor()
            .key("threadedWorldGen.reduceLockRadius")
            .comment("Whether to allow reducing lock radius \n" +
                    "(may cause incompatibility with other mods)")
            .getBoolean(true, false);

    public static final boolean asyncScheduling = new ConfigSystem.ConfigAccessor()
            .key("threadedWorldGen.asyncScheduling")
            .comment("""
                    Whether to enable async and parallel scheduling\s
                    This will reduce server thread load\s
                    (may cause incompatibility with other mods)
                    """)
            .getBoolean(true, false);

    public static void init() {
    }

}
