package com.ishland.c2me.mixin.optimization.worldgen.global_biome_cache;

import com.ishland.c2me.common.optimization.worldgen.global_biome_cache.BiomeCache;
import com.ishland.c2me.common.optimization.worldgen.global_biome_cache.IGlobalBiomeCache;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.layer.BiomeLayers;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(VanillaLayeredBiomeSource.class)
public abstract class MixinVanillaLayeredBiomeSource extends BiomeSource implements IGlobalBiomeCache {

    protected MixinVanillaLayeredBiomeSource(List<Biome> biomes) {
        super(biomes);
    }

    private BiomeCache cacheImpl = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(long seed, boolean legacyBiomeInitLayer, boolean largeBiomes, Registry<Biome> biomeRegistry, CallbackInfo info) {
        final ThreadLocal<BiomeLayerSampler> samplerThreadLocal = ThreadLocal.withInitial(() -> BiomeLayers.build(seed, legacyBiomeInitLayer, largeBiomes ? 6 : 4, 4));
        this.cacheImpl = new BiomeCache((biomeRegistry1, x, y, z) -> samplerThreadLocal.get().sample(biomeRegistry1, x, z), biomeRegistry, biomes);
    }

    /**
     * @author ishland
     * @reason re-implement caching
     */
    @Overwrite
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        return this.cacheImpl.getBiomeForNoiseGen(biomeX, biomeY, biomeZ, false);
    }

    @Override
    public Biome getBiomeForNoiseGenFast(int biomeX, int biomeY, int biomeZ) {
        return this.cacheImpl.getBiomeForNoiseGen(biomeX, biomeY, biomeZ, true);
    }

    @Override
    public BiomeArray preloadBiomes(HeightLimitView view, ChunkPos pos, BiomeArray def) {
        return cacheImpl.preloadBiomes(view, pos, def);
    }

}
