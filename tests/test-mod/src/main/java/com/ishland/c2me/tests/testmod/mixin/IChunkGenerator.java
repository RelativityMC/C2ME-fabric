package com.ishland.c2me.tests.testmod.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkGenerator.class)
public interface IChunkGenerator {

    @Accessor
    BiomeSource getPopulationSource();

    @Invoker("method_38263")
    boolean hasStructureFeature(ServerWorld serverWorld, StructureFeature<?> structureFeature);

}
