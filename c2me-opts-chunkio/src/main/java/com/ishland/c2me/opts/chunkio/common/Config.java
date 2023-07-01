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

    public static final long chunkStreamVersion = new ConfigSystem.ConfigAccessor()
            .key("ioSystem.chunkStreamVersion")
            .comment("""
                    Defines which chunk compression should be used\s
                    -1 for Vanilla default \s
                    1  for GZip (RFC1952) (Vanilla compatible)\s
                    2  for Zlib (RFC1950) (Vanilla default) (Vanilla compatible)\s
                    3  for Uncompressed (Fastest, but higher disk usage) (Vanilla compatible)\s
                    4  for zstd (Experimental, purposed for tests) (Vanilla Incompatible, won't loads worlds saved with this option, without this setting)\\s
                    \s
                    Original chunk data will still readable after modifying this option \s
                    as this option only affects newly stored chunks\s
                    Invalid values will fall back to vanilla default
                    """)
            .getLong(-1, -1);

    public static void init() {
    }

}
