package com.ishland.c2me.fixes.worldgen.threading_issues;

import com.ishland.c2me.fixes.worldgen.threading_issues.common.Config;

public class ModuleEntryPoint {

    private static final boolean enabled = true;

    static {
        Config.init();
    }

}
