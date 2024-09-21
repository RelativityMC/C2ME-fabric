package com.ishland.c2me.fixes.worldgen.vanilla_bugs.common;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class Config {

    public static final boolean suppressGhostMushrooms = new ConfigSystem.ConfigAccessor()
            .key("fixes.suppressGhostMushrooms")
            .comment("""
                    This option workarounds MC-276863, a bug that makes mushrooms appear in non-postprocessed chunks
                    This bug is amplified with notickvd as it exposes non-postprocessed chunks to players
                    
                    This should not affect other worldgen behavior and game mechanics in general
                    """)
            .getBoolean(true, false);

    public static void init() {
        // intentionally empty
    }

}
