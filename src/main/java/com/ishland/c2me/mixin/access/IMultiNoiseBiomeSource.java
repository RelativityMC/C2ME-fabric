package com.ishland.c2me.mixin.access;

import net.minecraft.class_6452;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiNoiseBiomeSource.class)
public interface IMultiNoiseBiomeSource {

    @Accessor
    class_6452.class_6455<Biome> getBiomePoints();

}
