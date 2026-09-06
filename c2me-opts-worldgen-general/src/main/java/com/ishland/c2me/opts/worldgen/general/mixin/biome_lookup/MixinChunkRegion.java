package com.ishland.c2me.opts.worldgen.general.mixin.biome_lookup;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRegion.class)
public abstract class MixinChunkRegion {

    @Inject(method = "getGeneratorStoredBiome", at = @At("HEAD"), cancellable = true)
    private void c2me$useStoredBiome(int biomeX, int biomeY, int biomeZ, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        ChunkRegion self = (ChunkRegion) (Object) this;
        Chunk chunk = self.getChunk(BiomeCoords.toChunk(biomeX), BiomeCoords.toChunk(biomeZ), ChunkStatus.EMPTY, false);
        if (chunk == null || !chunk.getStatus().isAtLeast(ChunkStatus.BIOMES)) return;
        cir.setReturnValue(chunk.getBiomeForNoiseGen(biomeX, biomeY, biomeZ));
    }

}
