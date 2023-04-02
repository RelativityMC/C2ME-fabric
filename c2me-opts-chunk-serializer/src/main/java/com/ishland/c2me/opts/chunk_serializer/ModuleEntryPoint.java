package com.ishland.c2me.opts.chunk_serializer;

import com.ishland.c2me.base.common.config.ConfigSystem;

public final class ModuleEntryPoint {

    @SuppressWarnings("unused")
    public static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("ioSystem.gcFreeChunkSerializer")
            .comment("""
                    EXPERIMENTAL FEATURE, DO NOT USE UNLESS YOU KNOW WHAT YOU ARE DOING
                    Do these before using this option
                    - disable lithium mixin.world.tick_scheduler
                    
                    Whether to use the fast reduced allocation chunk serializer
                    (may cause incompatibility with other mods)
                    """)
            .getBoolean(false, false);

}
