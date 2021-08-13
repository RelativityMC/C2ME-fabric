package com.ishland.c2me.compatibility.mixin.betterend;

import org.spongepowered.asm.mixin.Mixin;
import ru.betterend.world.features.BiomeIslandFeature;
import ru.betterend.world.features.FullHeightScatterFeature;
import ru.betterend.world.features.InvertedScatterFeature;
import ru.betterend.world.features.ScatterFeature;
import ru.betterend.world.features.SilkMothNestFeature;
import ru.betterend.world.features.UnderwaterPlantScatter;
import ru.betterend.world.features.terrain.DesertLakeFeature;
import ru.betterend.world.features.terrain.EndLakeFeature;
import ru.betterend.world.features.terrain.SulphuricLakeFeature;

@Mixin({
        DesertLakeFeature.class,
        EndLakeFeature.class,
        FullHeightScatterFeature.class,
        InvertedScatterFeature.class,
        ScatterFeature.class,
        SilkMothNestFeature.class,
        SulphuricLakeFeature.class,
        UnderwaterPlantScatter.class,
        BiomeIslandFeature.class
})
public class MixinASMTargets {
}
