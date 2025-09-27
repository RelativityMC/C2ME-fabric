package com.ishland.c2me.notickvd;

import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.notickvd.common.Config;

public class ModuleEntryPoint {

    public static final boolean enabled = !Boolean.getBoolean("com.ishland.c2me.notickvd.disable");

    static {
        Config.init();
    }

}
