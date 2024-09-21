package com.ishland.c2me.fixes.worldgen.vanilla_bugs;

import com.ishland.c2me.fixes.worldgen.vanilla_bugs.common.Config;

public class ModuleEntryPoint {

    private static final boolean enabled = true;

    static {
        Config.init();
    }

}
