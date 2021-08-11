package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import com.ishland.c2me.common.util.ListIndexedIterable;
import com.ishland.c2me.mixin.access.IMultiNoiseBiomeSource;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import java.util.List;
import java.util.Optional;

public class GlobalCachingMultiNoiseBiomeSource extends MultiNoiseBiomeSource implements IGlobalBiomeCache {

    private volatile BiomeCache biomeCache = null;

    public GlobalCachingMultiNoiseBiomeSource(long l, MultiNoiseUtil.Entries arg, Optional<Pair<Registry<Biome>, Preset>> optional) {
        super(l, arg, optional);
        initCache();
    }

    public GlobalCachingMultiNoiseBiomeSource(long l, MultiNoiseUtil.Entries<Biome> arg, NoiseParameters noiseParameters, NoiseParameters noiseParameters2, NoiseParameters noiseParameters3, NoiseParameters noiseParameters4, NoiseParameters noiseParameters5, int i, int j, boolean bl, Optional<Pair<Registry<Biome>, Preset>> optional) {
        super(l, arg, noiseParameters, noiseParameters2, noiseParameters3, noiseParameters4, noiseParameters5, i, j, bl, optional);
        initCache();
    }

    private void initCache() {
        final List<Biome> biomes = ((IMultiNoiseBiomeSource) this).getBiomeEntries().getEntries().stream()
                .map(pair -> pair.getSecond().get()).toList();
        this.biomeCache = new BiomeCache(super::getBiomeForNoiseGen, new ListIndexedIterable<>(biomes));
    }

    @Override
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        if (this.biomeCache != null) {
            return this.biomeCache.getBiomeForNoiseGen(biomeX, biomeY, biomeZ, false);
        } else {
            return super.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
        }
    }

    @Override
    public Biome getBiomeForNoiseGenFast(int biomeX, int biomeY, int biomeZ) {
        if (this.biomeCache != null) {
            return this.biomeCache.getBiomeForNoiseGen(biomeX, biomeY, biomeZ, true);
        } else {
            return super.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
        }
    }

    @Override
    public BiomeArray preloadBiomes(HeightLimitView view, ChunkPos pos, BiomeArray def) {
        if (this.biomeCache != null) {
            return this.biomeCache.preloadBiomes(view, pos, def);
        } else {
            throw new IllegalStateException("Not initialized");
        }
    }
}
