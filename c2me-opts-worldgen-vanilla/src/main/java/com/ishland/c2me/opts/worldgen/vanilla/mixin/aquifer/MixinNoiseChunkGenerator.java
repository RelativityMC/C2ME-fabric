package com.ishland.c2me.opts.worldgen.vanilla.mixin.aquifer;

import net.minecraft.block.Blocks;
import net.minecraft.util.registry.RegistryEntry;
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

@Mixin(NoiseChunkGenerator.class)
public class MixinNoiseChunkGenerator {

    @Shadow @Final protected RegistryEntry<ChunkGeneratorSettings> settings;

    @Mutable
    @Shadow @Final private AquiferSampler.FluidLevelSampler fluidLevelSampler;

    @Inject(method = "<init>(Lnet/minecraft/util/registry/Registry;Lnet/minecraft/util/registry/Registry;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/util/registry/RegistryEntry;)V", at = @At("RETURN"))
    private void modifyFluidLevelSampler(CallbackInfo ci) {
        // TODO [VanillaCopy]
        ChunkGeneratorSettings chunkGeneratorSettings = this.settings.value();
        AquiferSampler.FluidLevel fluidLevel = new AquiferSampler.FluidLevel(-54, Blocks.LAVA.getDefaultState());
        int i = chunkGeneratorSettings.seaLevel();
        AquiferSampler.FluidLevel fluidLevel2 = new AquiferSampler.FluidLevel(i, chunkGeneratorSettings.defaultFluid());
        final int min = Math.min(-54, i);
        this.fluidLevelSampler = (j, k, lx) -> k < min ? fluidLevel : fluidLevel2;
    }

}
