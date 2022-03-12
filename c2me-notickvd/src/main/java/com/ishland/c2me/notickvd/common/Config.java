package com.ishland.c2me.notickvd.common;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class Config {

    public static final int updatesPerTick = (int) new ConfigSystem.ConfigAccessor()
            .key("noTickViewDistance.updatesPerTick")
            .comment("No-tick view distance updates per tick \n" +
                    " Lower this for a better latency and higher this for a faster loading")
            .getLong(12, 12, ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY);

    public static final boolean compatibilityMode = new ConfigSystem.ConfigAccessor()
            .key("noTickViewDistance.compatibilityMode")
            .comment("Whether to use compatibility mode to send chunks \n" +
                    " This may fix some mod compatibility issues")
            .getBoolean(true, true);

    public static final boolean ensureChunkCorrectness = new ConfigSystem.ConfigAccessor()
            .key("noTickViewDistance.ensureChunkCorrectness")
            .comment("Whether to ensure correct chunks within normal render distance \n" +
                    " This will send chunks twice increasing network load")
            .getBoolean(false, true);

    public static void init() {
    }

}
