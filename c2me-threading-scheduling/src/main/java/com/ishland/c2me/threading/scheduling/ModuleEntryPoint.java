package com.ishland.c2me.threading.scheduling;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class ModuleEntryPoint {

    private static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("asyncScheduling.enabled")
            .comment("""
                    Whether to enable async and parallel scheduling\s
                    This will reduce server thread load\s
                    (may cause incompatibility with other mods)
                    """)
            .getBoolean(true, false);

}
