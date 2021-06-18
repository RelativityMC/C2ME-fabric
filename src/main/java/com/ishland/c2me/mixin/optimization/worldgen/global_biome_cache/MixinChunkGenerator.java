package com.ishland.c2me.mixin.optimization.worldgen.global_biome_cache;

import com.ishland.c2me.common.optimization.worldgen.global_biome_cache.IVanillaLayeredBiomeSource;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public class MixinChunkGenerator {

    @Shadow @Final protected BiomeSource biomeSource;

    @Inject(method = "populateBiomes", at = @At("HEAD"), cancellable = true)
    private void onPopulateBiomes(Registry<Biome> biomeRegistry, Chunk chunk, CallbackInfo ci) {
        if (biomeSource instanceof IVanillaLayeredBiomeSource biomeSource1) {
            ((ProtoChunk) chunk).setBiomes(biomeSource1.preloadBiomes(chunk, chunk.getPos(), chunk.getBiomeArray()));
            ci.cancel();
        }
    }

}
