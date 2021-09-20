package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import com.ishland.c2me.common.util.ListIndexedIterable;
import com.ishland.c2me.mixin.access.IMultiNoiseBiomeSource;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.List;
import java.util.Optional;

public class GlobalCachingMultiNoiseBiomeSource extends MultiNoiseBiomeSource implements IGlobalBiomeCache {

    private volatile MultiBiomeCache multiBiomeCache = null;

    public GlobalCachingMultiNoiseBiomeSource(MultiNoiseUtil.Entries<Biome> entries) {
        super(entries);
        initCache();
    }

    public GlobalCachingMultiNoiseBiomeSource(MultiNoiseUtil.Entries<Biome> entries, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Pair<Registry<Biome>, Preset>> optional) {
        super(entries, optional);
        initCache();
    }

    private void initCache() {
        final List<Biome> biomes = ((IMultiNoiseBiomeSource) this).getBiomeEntries().getEntries().stream()
                .map(pair -> pair.getSecond().get()).toList();
        this.multiBiomeCache = new MultiBiomeCache(super::method_38109, new ListIndexedIterable<>(biomes));
    }

    @Override
    public Biome method_38109(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        if (this.multiBiomeCache != null && !multiNoiseSampler.getClass().isSynthetic()) {
            return this.multiBiomeCache.getBiomeForNoiseGen(biomeX, biomeY, biomeZ, multiNoiseSampler, false);
        } else {
            return super.method_38109(biomeX, biomeY, biomeZ, multiNoiseSampler);
        }
    }

    @Override
    public Biome getBiomeForNoiseGenFast(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        if (this.multiBiomeCache != null) {
            return this.multiBiomeCache.getBiomeForNoiseGen(biomeX, biomeY, biomeZ, multiNoiseSampler, true);
        } else {
            return super.method_38109(biomeX, biomeY, biomeZ, multiNoiseSampler);
        }
    }

    @Override
    public Biome[][][] preloadBiomes(ChunkSectionPos pos, Biome[][][] def, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        if (this.multiBiomeCache != null) {
            return this.multiBiomeCache.preloadBiomes(pos, def, multiNoiseSampler);
        } else {
            throw new IllegalStateException("Not initialized");
        }
    }
}
