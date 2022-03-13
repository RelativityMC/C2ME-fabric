package com.ishland.c2me.opts.scheduling;

import com.ishland.c2me.opts.scheduling.common.Config;

public class ModuleEntryPoint {

    private static final boolean enabled = true;

    static {
        Config.init();
    }

}
