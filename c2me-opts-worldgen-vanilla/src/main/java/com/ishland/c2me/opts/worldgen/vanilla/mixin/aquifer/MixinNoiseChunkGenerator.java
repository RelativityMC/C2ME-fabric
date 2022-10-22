package com.ishland.c2me.opts.worldgen.vanilla.mixin.aquifer;

import net.minecraft.block.Blocks;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(NoiseChunkGenerator.class)
public class MixinNoiseChunkGenerator {

    /**
     * @author ishland
     * @reason optimize
     */
    @Overwrite
    private static AquiferSampler.FluidLevelSampler method_45510(ChunkGeneratorSettings chunkGeneratorSettings) {
        AquiferSampler.FluidLevel fluidLevel = new AquiferSampler.FluidLevel(-54, Blocks.LAVA.getDefaultState());
        int i = chunkGeneratorSettings.seaLevel();
        AquiferSampler.FluidLevel fluidLevel2 = new AquiferSampler.FluidLevel(i, chunkGeneratorSettings.defaultFluid());
        final int min = Math.min(-54, i);
        final AquiferSampler.FluidLevelSampler sampler = (j, k, lx) -> k < min ? fluidLevel : fluidLevel2;
        return sampler;
    }

}
