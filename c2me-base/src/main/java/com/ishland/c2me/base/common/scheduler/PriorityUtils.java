package com.ishland.c2me.base.common.scheduler;

import com.ishland.c2me.base.mixin.access.IChunkTicketManager;
import com.ishland.c2me.base.mixin.access.IChunkTicketManagerNearbyChunkTicketUpdater;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

public class PriorityUtils {

    static final Logger LOGGER = LoggerFactory.getLogger("C2ME Priority System");

    // int32 S0000000 000MNNNN LLLLLLLL DDDDDDDD
    // S: sign bit always 0
    // M: clear if in sync load range and set if not
    // N: distance to sync load chunk
    // L: load level
    // D: distance to nearest player

    private static final int BITS_8 = 0b11111111;
    private static final int BITS_4 = 0b111;

    private static final AtomicInteger priorityChanges = new AtomicInteger(0);

    public static IntSupplier getChunkPriority(ServerWorld serverWorld, @Nullable ChunkHolder holder, ChunkPos chunkPos) {
        final ServerChunkManager chunkManager = serverWorld.getChunkManager();
        final ISyncLoadManager syncLoadManager = (ISyncLoadManager) chunkManager;
        final ChunkTicketManager ticketManager = chunkManager.threadedAnvilChunkStorage.getTicketManager();
        final IChunkTicketManagerNearbyChunkTicketUpdater nearbyChunkTicketUpdater = (IChunkTicketManagerNearbyChunkTicketUpdater) ((IChunkTicketManager) ticketManager).getNearbyChunkTicketUpdater();
        final Long2IntMap distanceFromPlayers = nearbyChunkTicketUpdater.getDistances();
        final long pos = chunkPos.toLong();
        if (holder == null) {
            LOGGER.warn("Failed to retrieve ChunkHolder for chunk {}, assuming load level to 0", chunkPos);
        }
        return () -> (syncLoadPriority(chunkPos, syncLoadManager) << 16)
                | (((holder != null ? holder.getLevel() : 0) & BITS_8) << 8)
                        | (distanceFromPlayers.get(pos) & BITS_8);
    }

    public static IntSupplier getChunkPriority(ServerWorld serverWorld, ChunkPos pos) {
        ChunkHolder chunkHolder = ThreadLocalWorldGenSchedulingState.getChunkHolder();
        if (chunkHolder == null) {
            final Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders = ((IThreadedAnvilChunkStorage) serverWorld.getChunkManager().threadedAnvilChunkStorage).getChunkHolders();
            chunkHolder = chunkHolders.get(pos.toLong());
        }
        return getChunkPriority(serverWorld, chunkHolder, pos);
    }

    private static byte syncLoadPriority(ChunkPos pos, ISyncLoadManager manager) {
        final ChunkPos currentSyncLoad = manager.getCurrentSyncLoad();
        if (currentSyncLoad == null) return 0b11111;
        final int distance = chebyshevDistance(pos, currentSyncLoad);
        if (distance > 0b1111) return 0b11111;
        return (byte) (distance & BITS_4);
    }

    private static int chebyshevDistance(ChunkPos one, ChunkPos another) {
        return (another != null && one != null) ? Math.min(Math.abs(one.x - another.x), Math.abs(one.z - another.z)) : Integer.MAX_VALUE;
    }

    public static void notifyPriorityChange() {
        priorityChanges.incrementAndGet();
    }

    public static int priorityChangeSerial() {
        return priorityChanges.get();
    }

}
