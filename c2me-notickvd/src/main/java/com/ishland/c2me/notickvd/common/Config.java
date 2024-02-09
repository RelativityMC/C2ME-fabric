package com.ishland.c2me.notickvd.common;

import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.common.config.ConfigSystem;

public class Config {

    public static final int maxConcurrentChunkLoads = (int) new ConfigSystem.ConfigAccessor()
            .key("noTickViewDistance.maxConcurrentChunkLoads")
            .comment("No-tick view distance max concurrent chunk loads \n" +
                    " Lower this for a better latency and higher this for a faster loading")
            .getLong(GlobalExecutors.GLOBAL_EXECUTOR_PARALLELISM * 2L, GlobalExecutors.GLOBAL_EXECUTOR_PARALLELISM * 2L, ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY);

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

    public static final long maxViewDistance = new ConfigSystem.ConfigAccessor()
            .key("noTickViewDistance.maxViewDistance")
            .comment("""
                    Maximum view distance for no-tick view distance\s
                    
                    This allows you to specify the maximum view distance that no-tick view distance can support.\s
                    The maximum supported is 1073741823 and the minimum that make sense is 32,\s
                    This option is purely to save memory, as it needs to reserve memory for the maximum view distance\s
                    
                    Note: on the client side, `clientSideConfig.modifyMaxVDConfig.maxViewDistance` should also\s
                    be increased to actually expose the view distance in video settings\s
                    
                    """)
            .getLong(2048, 2048, ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY);

    public static void init() {
    }

}
