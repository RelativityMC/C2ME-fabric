package com.ishland.c2me.mixin.optimization.worldgen.thread_local_biome_cache;

import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.layer.BiomeLayers;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VanillaLayeredBiomeSource.class, priority = 1050)
public class MixinVanillaLayeredBiomeSource {

    private ThreadLocal<BiomeLayerSampler> biomeLayerSamplerThreadLocal = new ThreadLocal<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(long seed, boolean legacyBiomeInitLayer, boolean largeBiomes, Registry<Biome> biomeRegistry, CallbackInfo ci) {
        biomeLayerSamplerThreadLocal = ThreadLocal.withInitial(() -> BiomeLayers.build(seed, legacyBiomeInitLayer, largeBiomes ? 6 : 4, 4));
    }

    @Redirect(method = "getBiomeForNoiseGen", at = @At(value = "FIELD", target = "Lnet/minecraft/world/biome/source/VanillaLayeredBiomeSource;biomeSampler:Lnet/minecraft/world/biome/source/BiomeLayerSampler;"))
    private BiomeLayerSampler redirectSamplerUsage(VanillaLayeredBiomeSource unused) {
        return biomeLayerSamplerThreadLocal.get();
    }

}
