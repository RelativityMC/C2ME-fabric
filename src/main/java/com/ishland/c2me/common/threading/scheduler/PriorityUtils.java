package com.ishland.c2me.common.threading.scheduler;

import com.ishland.c2me.mixin.access.IChunkTicketManager;
import com.ishland.c2me.mixin.access.IChunkTicketManagerNearbyChunkTicketUpdater;
import com.ishland.c2me.mixin.access.IThreadedAnvilChunkStorage;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;

public class PriorityUtils {

    private static final int BITS_8 = 0b11111111;

    public static IntSupplier getChunkPriority(ServerWorld serverWorld, @Nullable ChunkHolder holder, ChunkPos chunkPos) {
        final ChunkTicketManager ticketManager = serverWorld.chunkManager.threadedAnvilChunkStorage.getTicketManager();
        final ChunkTicketManager.NearbyChunkTicketUpdater nearbyChunkTicketUpdater = ((IChunkTicketManager) ticketManager).getNearbyChunkTicketUpdater();
        final Long2IntMap distanceFromPlayers = ((IChunkTicketManagerNearbyChunkTicketUpdater) nearbyChunkTicketUpdater).getDistances();
        final long pos = chunkPos.toLong();
        if (holder == null) {
            SchedulerThread.LOGGER.warn("Failed to retrieve ChunkHolder for chunk {}, assuming load level to 0", chunkPos);
        }
        return () -> (((holder != null ? holder.getLevel() : 0) & BITS_8) << 8)
                | (distanceFromPlayers.get(pos) & BITS_8);
    }

    public static IntSupplier getChunkPriority(ServerWorld serverWorld, Chunk chunk) {
        final Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders = ((IThreadedAnvilChunkStorage) serverWorld.chunkManager.threadedAnvilChunkStorage).getChunkHolders();
        ChunkHolder chunkHolder = chunkHolders.get(chunk.getPos().toLong());
        return getChunkPriority(serverWorld, chunkHolder, chunk.getPos());
    }

}
