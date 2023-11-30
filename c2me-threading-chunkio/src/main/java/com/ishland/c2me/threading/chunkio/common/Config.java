package com.ishland.c2me.threading.chunkio.common;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class Config {

    public static final boolean recoverFromErrors = new ConfigSystem.ConfigAccessor()
            .key("ioSystem.recoverFromErrors")
            .comment("""
                    Whether to recover from errors when loading chunks\s
                     This will cause errored chunk to be regenerated entirely, which may cause data loss\s
                     Only applies when async chunk loading is enabled
                     """)
            .getBoolean(false, false);

    public static void init() {
    }

}
