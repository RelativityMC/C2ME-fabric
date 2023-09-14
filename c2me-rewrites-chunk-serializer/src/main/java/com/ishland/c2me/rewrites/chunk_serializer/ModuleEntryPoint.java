package com.ishland.c2me.rewrites.chunk_serializer;

import com.ishland.c2me.base.common.config.ConfigSystem;

public final class ModuleEntryPoint {

    @SuppressWarnings("unused")
    public static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("ioSystem.gcFreeChunkSerializer")
            .comment("""
                    EXPERIMENTAL FEATURE
                    This replaces the way your chunks are saved.
                    Please keep regular backups of your world if you are using this feature,
                    and report any world issues you encounter with this feature to our GitHub.
                    
                    Whether to use the fast reduced allocation chunk serializer
                    (may cause incompatibility with other mods)
                    """)
            .incompatibleMod("architectury", "*")
            .getBoolean(false, false);

}
