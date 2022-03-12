package com.ishland.c2me.client.uncapvd.common;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class Config {

    public static final int maxViewDistance = (int) new ConfigSystem.ConfigAccessor()
            .key("clientSideConfig.modifyMaxVDConfig.maxViewDistance")
            .comment("Max render distance allowed in game options")
            .getLong(128, 128, ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY);

    public static void init() {
    }

}
