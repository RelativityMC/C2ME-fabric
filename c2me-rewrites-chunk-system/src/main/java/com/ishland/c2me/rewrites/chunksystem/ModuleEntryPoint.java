package com.ishland.c2me.rewrites.chunksystem;

import com.ishland.c2me.rewrites.chunksystem.common.Config;

public class ModuleEntryPoint {

    private static final boolean enabled = true;

    static {
        Config.init();
    }

}
