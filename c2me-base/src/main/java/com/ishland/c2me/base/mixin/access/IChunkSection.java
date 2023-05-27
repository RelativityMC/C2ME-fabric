package com.ishland.c2me.base.mixin.access;

import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ReadableContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkSection.class)
public interface IChunkSection {
    @Accessor
    PalettedContainer<BlockState> getBlockStateContainer();

    @Accessor
    ReadableContainer<RegistryEntry<Biome>> getBiomeContainer();
}
