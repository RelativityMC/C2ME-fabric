package com.ishland.c2me.mixin.optimization.worldgen.global_biome_cache;

import com.ishland.c2me.common.optimization.worldgen.global_biome_cache.IGlobalBiomeCache;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BiomeSource.class)
public class MixinBiomeSource {

    @Redirect(method = {"getBiomesInArea", "locateBiome(IIIIILjava/util/function/Predicate;Ljava/util/Random;ZLnet/minecraft/world/biome/source/util/MultiNoiseUtil$MultiNoiseSampler;)Lnet/minecraft/util/math/BlockPos;"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/source/BiomeSource;getBiome(IIILnet/minecraft/world/biome/source/util/MultiNoiseUtil$MultiNoiseSampler;)Lnet/minecraft/world/biome/Biome;"))
    private Biome redirectGetBiomeForNoiseGen(BiomeSource biomeSource, int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        if (biomeSource instanceof IGlobalBiomeCache globalBiomeCache) {
            return globalBiomeCache.getBiomeForNoiseGenFast(biomeX, biomeY, biomeZ, multiNoiseSampler);
        }
        return biomeSource.getBiome(biomeX, biomeY, biomeZ, multiNoiseSampler);
    }

}
