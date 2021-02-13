package org.yatopiamc.barium.mixin.threading;

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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.yatopiamc.barium.common.mixininterface.IServerWorld;
import org.yatopiamc.barium.common.threading.ThreadingUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(ChunkStatus.class)
public class MixinChunkStatus {

    @Redirect(
            method = "runGenerationTask",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkStatus$GenerationTask;doWork(Lnet/minecraft/world/chunk/ChunkStatus;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/structure/StructureManager;Lnet/minecraft/server/world/ServerLightingProvider;Ljava/util/function/Function;Ljava/util/List;Lnet/minecraft/world/chunk/Chunk;)Ljava/util/concurrent/CompletableFuture;")
    )
    private CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> onRunGenerationTask(ChunkStatus.GenerationTask generationTask, ChunkStatus targetStatus, ServerWorld world, ChunkGenerator generator, StructureManager structureManager, ServerLightingProvider lightingProvider, Function<Chunk, CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> function, List<Chunk> surroundingChunks, Chunk chunk) {
        return ThreadingUtils.getThreadingType(targetStatus).runTask(((IServerWorld) world).getWorldGenSingleThreadedLock(), () -> generationTask.doWork(targetStatus, world, generator, structureManager, lightingProvider, function, surroundingChunks, chunk));
    }

}
