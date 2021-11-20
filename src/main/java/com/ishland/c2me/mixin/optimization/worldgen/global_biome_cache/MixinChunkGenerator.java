package com.ishland.c2me.mixin.optimization.worldgen.global_biome_cache;

import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ChunkGenerator.class)
public class MixinChunkGenerator {

    @Shadow @Final protected BiomeSource biomeSource;

    @Inject(method = "populateBiomes", at = @At("HEAD"), cancellable = true)
    private void onPopulateBiomes(Executor executor, Blender arg, StructureAccessor structureAccessor, Chunk chunk, CallbackInfoReturnable<CompletableFuture<Chunk>> cir) {
//        if (biomeSource instanceof IGlobalBiomeCache biomeSource1) {
////            ((ProtoChunk) chunk).setBiomes(biomeSource1.preloadBiomes(chunk.getPos(), chunk.getBiomeArray()));
//            cir.setReturnValue(CompletableFuture.completedFuture(chunk));
//        }
    }

}
