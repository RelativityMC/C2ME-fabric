package com.ishland.c2me.opts.chunk_access.mixin.region_capture;

import com.ishland.c2me.opts.chunk_access.common.CurrentWorldGenState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.GenerationTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(value = ChunkGenerationStep.class, priority = 1200)
public abstract class MixinChunkGenerationStep {

    @WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/GenerationTask;doWork(Lnet/minecraft/world/chunk/ChunkGenerationContext;Lnet/minecraft/world/chunk/ChunkGenerationStep;Lnet/minecraft/util/collection/BoundedRegionArray;Lnet/minecraft/world/chunk/Chunk;)Ljava/util/concurrent/CompletableFuture;"))
    public CompletableFuture<Chunk> runGenerationTask(GenerationTask instance, ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk, Operation<CompletableFuture<Chunk>> original) {
        final ChunkRegion chunkRegion = new ChunkRegion(context.world(), chunks, step, chunk);
        try {
            CurrentWorldGenState.setCurrentRegion(chunkRegion);
            return original.call(instance, context, step, chunks, chunk);
        } finally {
            CurrentWorldGenState.clearCurrentRegion();
        }
    }

}
