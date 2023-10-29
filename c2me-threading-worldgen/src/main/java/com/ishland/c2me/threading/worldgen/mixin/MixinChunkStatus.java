package com.ishland.c2me.threading.worldgen.mixin;

import com.ishland.c2me.base.common.scheduler.IVanillaChunkManager;
import com.ishland.c2me.base.common.scheduler.ThreadLocalWorldGenSchedulingState;
import com.ishland.c2me.base.common.util.SneakyThrow;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.opts.chunk_access.common.CurrentWorldGenState;
import com.ishland.c2me.threading.worldgen.common.ChunkStatusUtils;
import com.ishland.c2me.threading.worldgen.common.Config;
import com.ishland.c2me.threading.worldgen.common.IChunkStatus;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.profiling.jfr.Finishable;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
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
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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

    @Shadow
    public static List<ChunkStatus> createOrderedList() {
        throw new AbstractMethodError();
    }

    @Shadow public abstract String toString();

    private int reducedTaskRadius = -1;

    public void calculateReducedTaskRadius() {
        if (this.taskMargin == 0) {
            this.reducedTaskRadius = 0;
        } else {
            for (int i = 0; i <= this.taskMargin; i++) {
                final ChunkStatus status = ChunkStatus.byDistanceFromFull(ChunkStatus.getDistanceFromFull((ChunkStatus) (Object) this) + i); // TODO [VanillaCopy] from TACS getRequiredStatusForGeneration
                if (status.getIndex() <= ChunkStatus.BIOMES.getIndex()) {
                    this.reducedTaskRadius = Math.min(this.taskMargin, Math.max(0, i - 1));
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
        for (ChunkStatus chunkStatus : createOrderedList()) {
            ((IChunkStatus) chunkStatus).calculateReducedTaskRadius();
        }
    }

    /**
     * @author ishland
     * @reason take over generation
     */
    @Overwrite
    public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> runGenerationTask(Executor executor, ServerWorld world, ChunkGenerator chunkGenerator, StructureTemplateManager structureManager, ServerLightingProvider lightingProvider, Function<Chunk, CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> function, List<Chunk> list) {
        final ChunkStatus thiz = (ChunkStatus) (Object) this;
        final Chunk targetChunk = list.get(list.size() / 2);

        Finishable finishable = FlightProfiler.INSTANCE.startChunkGenerationProfiling(targetChunk.getPos(), world.getRegistryKey(), this.toString());

        final Supplier<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> generationTask = () -> {
            try {
                CurrentWorldGenState.setCurrentRegion(new ChunkRegion(world, list, thiz, -1));
                return this.generationTask.doWork(thiz, executor, world, chunkGenerator, structureManager, lightingProvider, function, list, targetChunk);
            } finally {
                CurrentWorldGenState.clearCurrentRegion();
            }
        };

        final CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture;

        if (targetChunk.getStatus().isAtLeast(thiz)) {
            completableFuture = generationTask.get();
        } else {
            final ChunkHolder holder = ThreadLocalWorldGenSchedulingState.getChunkHolder();
            final ThreadedAnvilChunkStorage tacs = world.getChunkManager().threadedAnvilChunkStorage;
            if (holder != null && ChunkStatusUtils.isCancelled(holder, thiz)) {
                completableFuture = ChunkHolder.UNLOADED_CHUNK_FUTURE;
                ((IThreadedAnvilChunkStorage) tacs).invokeReleaseLightTicket(targetChunk.getPos()); // vanilla behavior
//                System.out.println(String.format("%s: %s is already done or cancelled, skipping generation", this, targetChunk.getPos()));
            } else {
                int lockRadius = Config.reduceLockRadius && this.reducedTaskRadius != -1 ? this.reducedTaskRadius : this.taskMargin;
                //noinspection ConstantConditions
                completableFuture = ChunkStatusUtils.runChunkGenWithLock(
                                targetChunk.getPos(),
                                thiz,
                                lockRadius,
                                ((IVanillaChunkManager) tacs).c2me$getSchedulingManager(),
                                ChunkStatusUtils.getThreadingType(thiz),
                                generationTask)
                        .exceptionally(t -> {
                            Throwable actual = t;
                            while (actual instanceof CompletionException) actual = t.getCause();
                            if (actual instanceof CancellationException) {
                                return ChunkHolder.UNLOADED_CHUNK;
                            } else {
                                SneakyThrow.sneaky(t);
                                return null; // unreachable
                            }
                        });

            }
        }

        completableFuture.exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });

        // TODO [VanillaCopy]
        return completableFuture.thenApply(either -> {
            if (either.left().isPresent()) {
                if (either.left().get() instanceof ProtoChunk protoChunk && !protoChunk.getStatus().isAtLeast(thiz)) {
                    protoChunk.setStatus(thiz);
                }
            }

            if (finishable != null) {
                finishable.finish();
            }
            return either;
        });
    }

}
