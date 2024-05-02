package com.ishland.c2me.opts.worldgen.vanilla.common;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class Config {

    public static final boolean optimizeAquifer = new ConfigSystem.ConfigAccessor()
            .key("vanillaWorldGenOptimizations.optimizeAquifer")
            .comment("Whether to enable aquifer optimizations to accelerate overworld worldgen\n" +
                    "(may cause incompatibility with other mods)")
            .incompatibleMod("cavetweaks", "*")
            .getBoolean(true, false);

    public static final boolean useEndBiomeCache = new ConfigSystem.ConfigAccessor()
            .key("vanillaWorldGenOptimizations.useEndBiomeCache")
            .comment("""
                    Whether to enable End Biome Cache to accelerate The End worldgen\s
                    This is no longer included in lithium-fabric\s
                    (may cause incompatibility with other mods)
                    """)
            .incompatibleMod("biolith", "*")
            .getBoolean(true, false);

}
