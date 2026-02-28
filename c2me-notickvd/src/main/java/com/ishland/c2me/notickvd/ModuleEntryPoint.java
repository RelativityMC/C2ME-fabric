package com.ishland.c2me.notickvd;

import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.notickvd.common.Config;

public class ModuleEntryPoint {

    public static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("noTickViewDistance.enabled")
            .comment("""
                    Whether to enable no-tick view distance
                    
                    Faster alternative implementation of simulation distance, and additionally support up to 65530 view distance
                    """)
            .getBoolean(true, false);

    static {
        Config.init();
    }

}
