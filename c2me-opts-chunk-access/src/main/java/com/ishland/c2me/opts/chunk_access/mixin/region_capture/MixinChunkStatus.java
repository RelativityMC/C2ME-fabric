package com.ishland.c2me.opts.chunk_access.mixin.region_capture;

import com.ishland.c2me.opts.chunk_access.common.CurrentWorldGenState;
import net.minecraft.util.function.Finishable;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.FullChunkConverter;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = ChunkStatus.class, priority = 990)
public abstract class MixinChunkStatus {

    @Shadow @Final private ChunkStatus.GenerationTask generationTask;

    @Shadow public abstract String toString();

    /**
     * @author ishland
     * @reason capture chunk regions
     */
    @Overwrite
    public CompletableFuture<Chunk> runGenerationTask(ChunkGenerationContext context, Executor executor, FullChunkConverter fullChunkConverter, List<Chunk> chunks) {
        final ChunkRegion chunkRegion = new ChunkRegion(context.world(), chunks, (ChunkStatus) (Object) this, 0);
        try {
            CurrentWorldGenState.setCurrentRegion(chunkRegion);
            Chunk chunk = chunks.get(chunks.size() / 2);
            Finishable finishable = FlightProfiler.INSTANCE.startChunkGenerationProfiling(chunk.getPos(), context.world().getRegistryKey(), this.toString());
            return this.generationTask.doWork(context, (ChunkStatus) (Object) this, executor, fullChunkConverter, chunks, chunk).thenApply(chunkx -> {
                if (chunkx instanceof ProtoChunk protoChunk && !protoChunk.getStatus().isAtLeast((ChunkStatus) (Object) this)) {
                    protoChunk.setStatus((ChunkStatus) (Object) this);
                }

                if (finishable != null) {
                    finishable.finish();
                }

                return chunkx;
            });
        } finally {
            CurrentWorldGenState.clearCurrentRegion();
        }
    }

}
