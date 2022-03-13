package com.ishland.c2me.opts.chunk_access;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class ModuleEntryPoint {

    public static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("generalOptimizations.optimizeAsyncChunkRequest")
            .comment("Whether to let async chunk request no longer block server thread\n" +
                    "(may cause incompatibility with other mods)")
            .getBoolean(true, false);

}
