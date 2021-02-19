package org.yatopiamc.c2me.mixin.threading.worldgen;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.yatopiamc.c2me.common.threading.worldgen.ChunkStatusUtils;
import org.yatopiamc.c2me.common.threading.worldgen.IWorldGenLockable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(ChunkStatus.class)
public class MixinChunkStatus {

    @Shadow
    @Final
    private ChunkStatus.GenerationTask generationTask;

    @Shadow @Final public static ChunkStatus FEATURES;

    /**
     * @author ishland
     * @reason take over generation
     */
    @Overwrite
    public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> runGenerationTask(ServerWorld world, ChunkGenerator chunkGenerator, StructureManager structureManager, ServerLightingProvider lightingProvider, Function<Chunk, CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> function, List<Chunk> chunks) {
        final Chunk targetChunk = chunks.get(chunks.size() / 2);
        //noinspection ConstantConditions
        return ChunkStatusUtils.runChunkGenWithLock(targetChunk.getPos(), (Object) this == FEATURES ? 1 : 0, ((IWorldGenLockable) world).getWorldGenChunkLock(), () ->
                ChunkStatusUtils.getThreadingType((ChunkStatus) (Object) this).runTask(((IWorldGenLockable) world).getWorldGenSingleThreadedLock(), () ->
                        this.generationTask.doWork((ChunkStatus) (Object) this, world, chunkGenerator, structureManager, lightingProvider, function, chunks, targetChunk)
                )
        );
    }

}
