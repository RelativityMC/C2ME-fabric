package com.ishland.c2me.base.mixin.access;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiNoiseBiomeSource.class)
public interface IMultiNoiseBiomeSource {

    @Accessor
    MultiNoiseUtil.Entries<Biome> getBiomeEntries();

}
