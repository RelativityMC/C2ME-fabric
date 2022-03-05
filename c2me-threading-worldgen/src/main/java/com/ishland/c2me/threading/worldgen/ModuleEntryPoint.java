package com.ishland.c2me.threading.worldgen;

import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.threading.worldgen.common.Config;

class ModuleEntryPoint {

    public static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("threadedWorldGen.enabled")
            .comment("Whether to enable this feature")
            .getBoolean(true, false);

    static {
        Config.init();
    }

}
