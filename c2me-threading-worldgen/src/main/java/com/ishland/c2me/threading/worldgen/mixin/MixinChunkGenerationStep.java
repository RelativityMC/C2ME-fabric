package com.ishland.c2me.threading.worldgen.mixin;

import com.ishland.c2me.base.common.scheduler.IVanillaChunkManager;
import com.ishland.c2me.threading.worldgen.common.ChunkStatusUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.GenerationTask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkGenerationStep.class)
public class MixinChunkGenerationStep {

    @Shadow
    @Final
    private ChunkStatus targetStatus;

    @WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/GenerationTask;doWork(Lnet/minecraft/world/chunk/ChunkGenerationContext;Lnet/minecraft/world/chunk/ChunkGenerationStep;Lnet/minecraft/util/collection/BoundedRegionArray;Lnet/minecraft/world/chunk/Chunk;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Chunk> wrapGenerationStep(GenerationTask instance, ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk, Operation<CompletableFuture<Chunk>> original) {
        if (chunk.getStatus().isEarlierThan(this.targetStatus)) {
            return ChunkStatusUtils.runChunkGenWithLock(
                    chunk.getPos(),
                    this.targetStatus,
                    step.blockStateWriteRadius(),
                    ((IVanillaChunkManager) context.world().getChunkManager().chunkLoadingManager).c2me$getSchedulingManager(),
                    ChunkStatusUtils.getThreadingType(this.targetStatus),
                    () -> original.call(instance, context, step, chunks, chunk)
            );
        } else {
            return original.call(instance, context, step, chunks, chunk);
        }
    }

}
