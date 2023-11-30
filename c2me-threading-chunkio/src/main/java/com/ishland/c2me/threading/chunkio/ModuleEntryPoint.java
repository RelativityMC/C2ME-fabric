package com.ishland.c2me.threading.chunkio;

import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.threading.chunkio.common.Config;

public class ModuleEntryPoint {

    private static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("ioSystem.async")
            .comment("Whether to use async chunk loading & unloading")
            .incompatibleMod("radon", "*")
            .getBoolean(true, false);

    static {
        Config.init();
    }

}
