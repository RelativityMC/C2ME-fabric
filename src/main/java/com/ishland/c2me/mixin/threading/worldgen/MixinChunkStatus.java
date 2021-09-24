package com.ishland.c2me.mixin.threading.worldgen;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.threading.worldgen.ChunkStatusUtils;
import com.ishland.c2me.common.threading.worldgen.IChunkStatus;
import com.ishland.c2me.common.threading.worldgen.IWorldGenLockable;
import com.mojang.datafixers.util.Either;
import net.minecraft.class_6611;
import net.minecraft.class_6613;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(ChunkStatus.class)
public abstract class MixinChunkStatus implements IChunkStatus {

    @Shadow
    @Final
    private ChunkStatus.GenerationTask generationTask;

    @Shadow
    @Final
    private int taskMargin;

    @Shadow @Final private String id;
    private int reducedTaskRadius = -1;

    public void calculateReducedTaskRadius() {
        if (this.taskMargin == 0) {
            this.reducedTaskRadius = 0;
        } else {
            for (int i = 0; i <= this.taskMargin; i++) {
                final ChunkStatus status = ChunkStatus.byDistanceFromFull(ChunkStatus.getDistanceFromFull((ChunkStatus) (Object) this) + i); // TODO [VanillaCopy] from TACS getRequiredStatusForGeneration
                if (status == ChunkStatus.STRUCTURE_STARTS) {
                    this.reducedTaskRadius = Math.min(this.taskMargin, i);
                    break;
                }
            }
        }
        //noinspection ConstantConditions
        if ((Object) this == ChunkStatus.LIGHT) {
            this.reducedTaskRadius = 1;
        }
        System.out.printf("%s task radius: %d -> %d%n", this, this.taskMargin, this.reducedTaskRadius);
    }

    @Override
    public int getReducedTaskRadius() {
        return this.reducedTaskRadius;
    }

    @Dynamic
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onCLInit(CallbackInfo info) {
        for (ChunkStatus chunkStatus : Registry.CHUNK_STATUS) {
            ((IChunkStatus) chunkStatus).calculateReducedTaskRadius();
        }
    }

    /**
     * @author ishland
     * @reason take over generation
     */
    @Overwrite
    public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> runGenerationTask(Executor executor, ServerWorld world, ChunkGenerator chunkGenerator, StructureManager structureManager, ServerLightingProvider lightingProvider, Function<Chunk, CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> function, List<Chunk> list, boolean bl) {
        final Chunk targetChunk = list.get(list.size() / 2);

        class_6613 lv = class_6611.field_34923.method_38655(targetChunk.getPos(), world.getRegistryKey(), this.id);

        final Supplier<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> generationTask = () ->
                this.generationTask.doWork((ChunkStatus) (Object) this, executor, world, chunkGenerator, structureManager, lightingProvider, function, list, targetChunk, bl);

        final CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture;
        if (targetChunk.getStatus().isAtLeast((ChunkStatus) (Object) this)) {
            completableFuture =  generationTask.get();
        } else {
            int lockRadius = C2MEConfig.threadedWorldGenConfig.reduceLockRadius && this.reducedTaskRadius != -1 ? this.reducedTaskRadius : this.taskMargin;
            //noinspection ConstantConditions
            completableFuture = ChunkStatusUtils.runChunkGenWithLock(targetChunk.getPos(), lockRadius, ((IWorldGenLockable) world).getWorldGenChunkLock(), () ->
                    ChunkStatusUtils.getThreadingType((ChunkStatus) (Object) this).runTask(((IWorldGenLockable) world).getWorldGenSingleThreadedLock(), generationTask));
        }

        completableFuture.exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });

        // TODO [VanillaCopy]
        return lv != null ? completableFuture.thenApply(either -> {
            lv.finish();
            return either;
        }) : completableFuture;
    }

}
