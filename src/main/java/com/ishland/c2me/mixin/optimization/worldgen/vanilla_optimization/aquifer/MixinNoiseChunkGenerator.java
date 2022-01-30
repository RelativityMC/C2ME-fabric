package com.ishland.c2me.mixin.optimization.worldgen.vanilla_optimization.aquifer;

import net.minecraft.block.Blocks;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(NoiseChunkGenerator.class)
public class MixinNoiseChunkGenerator {

    @Shadow @Final protected Supplier<ChunkGeneratorSettings> settings;

    @Mutable
    @Shadow @Final private AquiferSampler.FluidLevelSampler fluidLevelSampler;

    @Inject(method = "<init>(Lnet/minecraft/util/registry/Registry;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;JLjava/util/function/Supplier;)V", at = @At("RETURN"))
    private void modifyFluidLevelSampler(CallbackInfo ci) {
        // TODO [VanillaCopy]
        ChunkGeneratorSettings chunkGeneratorSettings = this.settings.get();
        AquiferSampler.FluidLevel fluidLevel = new AquiferSampler.FluidLevel(-54, Blocks.LAVA.getDefaultState());
        int i = chunkGeneratorSettings.getSeaLevel();
        AquiferSampler.FluidLevel fluidLevel2 = new AquiferSampler.FluidLevel(i, chunkGeneratorSettings.getDefaultFluid());
        final int min = Math.min(-54, i);
        this.fluidLevelSampler = (j, k, l) -> k < min ? fluidLevel : fluidLevel2; // reduce branching
    }

}
