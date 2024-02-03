package com.ishland.c2me.opts.chunkio.common;

import com.ishland.c2me.base.common.config.ConfigSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {

    static final Logger LOGGER = LoggerFactory.getLogger("C2ME Opts/ChunkIO Config");

    public static final long chunkDataCacheSoftLimit = new ConfigSystem.ConfigAccessor()
            .key("ioSystem.chunkDataCacheSoftLimit")
            .comment("Soft limit for io worker nbt cache")
            .getLong(8192, 8192, ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY);

    public static final long chunkDataCacheLimit = new ConfigSystem.ConfigAccessor()
            .key("ioSystem.chunkDataCacheLimit")
            .comment("Hard limit for io worker nbt cache")
            .getLong(32678, 32678, ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY);

    public static void init() {
    }

}
