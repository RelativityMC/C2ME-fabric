package com.ishland.c2me.threading.worldgen;

import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.threading.worldgen.common.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ModuleEntryPoint {

    public static final Logger LOGGER = LoggerFactory.getLogger("C2ME Threaded WorldGen");

    public static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("threadedWorldGen.enabled")
            .comment("""
                    Whether to enable this feature\s
                     This feature may cause regression when worker parallelism is lower than 4.
                     """)
            .getBoolean(GlobalExecutors.GLOBAL_EXECUTOR_PARALLELISM >= 4, false);

    static {
        Config.init();
        if (enabled && GlobalExecutors.GLOBAL_EXECUTOR_PARALLELISM < 4) {
            LOGGER.warn("WARNING: Global worker parallelism is lower than 4 and threaded worldgen is enabled. This may cause regression in worldgen performance");
        }
    }

}
