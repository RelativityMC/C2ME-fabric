package com.ishland.c2me.opts.chunkio;

import com.ishland.c2me.opts.chunkio.common.Config;

public class ModuleEntryPoint {

    private static final boolean enabled = true;

    static {
        Config.init();
    }

}
