package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.source.BiomeArray;

public interface IBiomePreloadable {

    BiomeArray preloadBiomes(HeightLimitView view, ChunkPos pos, BiomeArray def);
}
