package org.yatopiamc.c2me.mixin.optimization.worldgen.global_biome_cache;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.layer.BiomeLayers;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.optimization.worldgen.global_biome_cache.BiomeCache;
import org.yatopiamc.c2me.common.optimization.worldgen.global_biome_cache.IVanillaLayeredBiomeSource;

import java.util.List;

@Mixin(VanillaLayeredBiomeSource.class)
public abstract class MixinVanillaLayeredBiomeSource extends BiomeSource implements IVanillaLayeredBiomeSource {

    @Shadow @Final private BiomeLayerSampler biomeSampler;

    @Shadow @Final private Registry<Biome> biomeRegistry;

    protected MixinVanillaLayeredBiomeSource(List<Biome> biomes) {
        super(biomes);
    }

    private BiomeCache cacheImpl = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(long seed, boolean legacyBiomeInitLayer, boolean largeBiomes, Registry<Biome> biomeRegistry, CallbackInfo info) {
        this.cacheImpl = new BiomeCache(ThreadLocal.withInitial(() -> BiomeLayers.build(seed, legacyBiomeInitLayer, largeBiomes ? 6 : 4, 4)), biomeRegistry, biomes);
    }

    /**
     * @author ishland
     * @reason re-implement caching
     */
    @Overwrite
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        return this.cacheImpl.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
    }

    @Override
    public BiomeArray preloadBiomes(HeightLimitView view, ChunkPos pos, BiomeArray def) {
        return cacheImpl.preloadBiomes(view, pos, def);
    }

}
