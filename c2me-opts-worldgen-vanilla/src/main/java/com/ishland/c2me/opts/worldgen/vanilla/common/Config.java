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

    public static final boolean optimizeStructureWeightSampler = new ConfigSystem.ConfigAccessor()
            .key("vanillaWorldGenOptimizations.optimizeStructureWeightSampler")
            .comment("""
                    Whether to enable StructureWeightSampler optimizations to accelerate world generation
                    """)
            .incompatibleMod("porting_lib", "*")
            .getBoolean(true, false);

}
