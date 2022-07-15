package com.ishland.c2me.opts.chunkio.common.async_chunk_on_player_login;

import com.ishland.c2me.base.mixin.access.IServerChunkManager;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.concurrent.CompletableFuture;

public class AsyncChunkLoadUtil {

    private static final ChunkTicketType<Unit> ASYNC_PLAYER_LOGIN = ChunkTicketType.create("async_player_login", (unit, unit2) -> 0);

    public static CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> scheduleChunkLoad(ServerWorld world, ChunkPos pos) {
        final ServerChunkManager chunkManager = world.getChunkManager();
        final ChunkTicketManager ticketManager = ((IServerChunkManager) chunkManager).getTicketManager();
        ticketManager.addTicket(ASYNC_PLAYER_LOGIN, pos, 3, Unit.INSTANCE);
        ((IServerChunkManager) chunkManager).invokeTick();
        final ChunkHolder chunkHolder = ((IThreadedAnvilChunkStorage) chunkManager.threadedAnvilChunkStorage).getCurrentChunkHolders().get(pos.toLong());
        if (chunkHolder == null) {
            throw new IllegalStateException("Chunk not there when requested");
        }
        final CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> future = chunkHolder.getEntityTickingFuture();
        future.whenCompleteAsync((unused, throwable) -> ticketManager.removeTicket(ASYNC_PLAYER_LOGIN, pos, 3, Unit.INSTANCE), world.getServer());
        return future;
    }

    private static final ThreadLocal<Boolean> isRespawnChunkLoadFinished = ThreadLocal.withInitial(() -> false);

    public static void setIsRespawnChunkLoadFinished(boolean value) {
        isRespawnChunkLoadFinished.set(value);
    }

    public static boolean isRespawnChunkLoadFinished() {
        return isRespawnChunkLoadFinished.get();
    }

}
