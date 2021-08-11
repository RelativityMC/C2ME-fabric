package com.ishland.c2me.common.optimization.worldgen.threadlocal_biome_cache;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import java.util.Optional;

public class ThreadLocalCachingMultiNoiseBiomeSource extends MultiNoiseBiomeSource {

    private final BiomeSourceCachingDelegate biomeSourceCachingDelegate = new BiomeSourceCachingDelegate(super::getBiomeForNoiseGen);

    public ThreadLocalCachingMultiNoiseBiomeSource(long l, MultiNoiseUtil.Entries arg, Optional<Pair<Registry<Biome>, Preset>> optional) {
        super(l, arg, optional);
    }

    public ThreadLocalCachingMultiNoiseBiomeSource(long l, MultiNoiseUtil.Entries<Biome> arg, NoiseParameters noiseParameters, NoiseParameters noiseParameters2, NoiseParameters noiseParameters3, NoiseParameters noiseParameters4, NoiseParameters noiseParameters5, int i, int j, boolean bl, Optional<Pair<Registry<Biome>, Preset>> optional) {
        super(l, arg, noiseParameters, noiseParameters2, noiseParameters3, noiseParameters4, noiseParameters5, i, j, bl, optional);
    }

    @Override
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        return biomeSourceCachingDelegate.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
    }

}
