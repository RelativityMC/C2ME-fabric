package com.ishland.c2me.base;

import com.ishland.c2me.base.common.config.ConfigSystem;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class C2MEBaseMod implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        ConfigSystem.flushConfig();
    }
}
