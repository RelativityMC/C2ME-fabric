package com.ishland.c2me.tests.testmod.mixin;

import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkGenerator.class)
public interface IChunkGenerator {

    @Accessor
    BiomeSource getPopulationSource();

}
