package com.ishland.c2me.tests.testmod.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkManager.class)
public interface IServerChunkManager {

    @Invoker
    boolean invokeUpdateChunks();

    @Invoker
    CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> invokeGetChunkFuture(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create);

}
