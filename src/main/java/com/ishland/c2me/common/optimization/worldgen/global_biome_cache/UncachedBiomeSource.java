package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import com.mojang.serialization.Codec;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;

import java.util.List;

public class UncachedBiomeSource extends BiomeSource {
    private final ThreadLocal<BiomeLayerSampler> sampler;
    private final Registry<Biome> registry;

    public UncachedBiomeSource(List<Biome> biomes, ThreadLocal<BiomeLayerSampler> sampler, Registry<Biome> registry) {
        super(biomes);
        this.sampler = sampler;
        this.registry = registry;
    }

    @Override
    protected Codec<? extends BiomeSource> getCodec() {
        return VanillaLayeredBiomeSource.CODEC;
    }

    @Override
    public BiomeSource withSeed(long seed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        return sampler.get().sample(this.registry, biomeX, biomeZ);
    }
}
