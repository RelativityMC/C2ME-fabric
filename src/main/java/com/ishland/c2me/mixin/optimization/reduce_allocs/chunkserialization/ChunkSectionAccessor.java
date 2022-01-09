package com.ishland.c2me.mixin.optimization.reduce_allocs.chunkserialization;

import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkSection.class)
public interface ChunkSectionAccessor {
    @Accessor
    PalettedContainer<BlockState> getBlockStateContainer();

    @Accessor
    PalettedContainer<Biome> getBiomeContainer();
}
