package org.yatopiamc.c2me.mixin.optimization.worldgen.threadlocal_noise_sampler;

import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.layer.BiomeLayers;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VanillaLayeredBiomeSource.class)
public class MixinVanillaLayeredBiomeSource {

    @Shadow @Final private Registry<Biome> biomeRegistry;
    private ThreadLocal<BiomeLayerSampler> threadLocalBiomeSampler = new ThreadLocal<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(long seed, boolean legacyBiomeInitLayer, boolean largeBiomes, Registry<Biome> biomeRegistry, CallbackInfo ci) {
        threadLocalBiomeSampler = ThreadLocal.withInitial(() -> BiomeLayers.build(seed, legacyBiomeInitLayer, largeBiomes ? 6 : 4, 4)); // [VanillaCopy]
    }

    /**
     * @author ishland
     * @reason use thread local biome sampler
     */
    @Overwrite
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        return this.threadLocalBiomeSampler.get().sample(this.biomeRegistry, biomeX, biomeZ);
    }

}
