package com.ishland.c2me.notickvd.common;

import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.notickvd.common.modimpl.ChunkPosDistanceLevelPropagatorExtended;
import com.ishland.c2me.notickvd.common.modimpl.LevelPropagatorExtended;
import com.ishland.flowsched.structs.DynamicPriorityQueue;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

public class PlayerNoTickDistanceMap extends ChunkPosDistanceLevelPropagatorExtended {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ChunkTicketType<ChunkPos> TICKET_TYPE = ChunkTicketType.create("c2me_no_tick_vd", Comparator.comparingLong(ChunkPos::toLong));
    public static final int MAX_RENDER_DISTANCE = MathHelper.clamp((int) Config.maxViewDistance, 32, LevelPropagatorExtended.MAX_LEVEL - 10);

    private final LongSet sourceChunks = new LongOpenHashSet();
    private final Long2IntOpenHashMap distanceFromNearestPlayer = new Long2IntOpenHashMap();
    private final DynamicPriorityQueue<ChunkPos> pendingTicketAdds = new DynamicPriorityQueue<>(MAX_RENDER_DISTANCE + 2);
    private final LongOpenHashSet pendingTicketRemoves = new LongOpenHashSet();
    private final LongOpenHashSet managedChunkTickets = new LongOpenHashSet();
    private final ReferenceArrayList<CompletableFuture<Void>> chunkLoadFutures = new ReferenceArrayList<>();

    private final ChunkTicketManager chunkTicketManager;
    private final NoTickSystem noTickSystem;
    private volatile int viewDistance;
    private volatile int pendingTicketUpdatesCount = 0; // for easier access concurrently

    public PlayerNoTickDistanceMap(ChunkTicketManager chunkTicketManager, NoTickSystem noTickSystem) {
        super(MAX_RENDER_DISTANCE + 2, 16, 256);
        this.chunkTicketManager = chunkTicketManager;
        this.noTickSystem = noTickSystem;
        this.distanceFromNearestPlayer.defaultReturnValue(MAX_RENDER_DISTANCE + 2);
        this.setViewDistance(12);
    }

    @Override
    protected int getInitialLevel(long chunkPos) {
        final ObjectSet<ServerPlayerEntity> players = ((com.ishland.c2me.base.mixin.access.IChunkTicketManager) chunkTicketManager).getPlayersByChunkPos().get(chunkPos);
        return players != null && !players.isEmpty() ? MAX_RENDER_DISTANCE - viewDistance : Integer.MAX_VALUE;
    }

    @Override
    protected int getLevel(long chunkPos) {
        return this.distanceFromNearestPlayer.get(chunkPos);
    }

    @Override
    protected void setLevel(long chunkPos, int level) {
        if (level > MAX_RENDER_DISTANCE) {
            if (this.distanceFromNearestPlayer.containsKey(chunkPos)) {
                this.pendingTicketRemoves.add(chunkPos);
                this.pendingTicketAdds.remove(new ChunkPos(chunkPos));
                this.distanceFromNearestPlayer.remove(chunkPos);
            }
        } else {
            if (!this.distanceFromNearestPlayer.containsKey(chunkPos)) {
                pendingTicketRemoves.remove(chunkPos);
                pendingTicketAdds.enqueue(new ChunkPos(chunkPos), level);
            }
            pendingTicketAdds.changePriority(new ChunkPos(chunkPos), level);
            this.distanceFromNearestPlayer.put(chunkPos, level);
        }
    }

    public void addSource(ChunkPos chunkPos) {
        this.updateLevel(chunkPos.toLong(), MAX_RENDER_DISTANCE - this.viewDistance, true);
        this.sourceChunks.add(chunkPos.toLong());
    }

    public void removeSource(ChunkPos chunkPos) {
        this.updateLevel(chunkPos.toLong(), Integer.MAX_VALUE, false);
        this.sourceChunks.remove(chunkPos.toLong());
    }

    public boolean update() {
        final boolean hasUpdates = this.applyPendingUpdates(Integer.MAX_VALUE) != Integer.MAX_VALUE;
        this.pendingTicketUpdatesCount = this.pendingTicketAdds.size() + this.pendingTicketRemoves.size();
        return hasUpdates;
    }

    private boolean hasPendingTicketUpdatesAsync = false;

    boolean runPendingTicketUpdates(ThreadedAnvilChunkStorage tacs) {
        final boolean hasUpdatesNow = runPendingTicketUpdatesInternal(tacs);
        final boolean hasUpdatesEarlier = hasPendingTicketUpdatesAsync;
        hasPendingTicketUpdatesAsync = false;
        return hasUpdatesNow || hasUpdatesEarlier;
    }

    private boolean runPendingTicketUpdatesInternal(ThreadedAnvilChunkStorage tacs) {
        boolean hasUpdates = false;
        // remove old tickets
        {
            final LongIterator it = pendingTicketRemoves.longIterator();
            while (it.hasNext()) {
                final long chunkPos = it.nextLong();
                if (this.managedChunkTickets.remove(chunkPos)) {
                    removeTicket0(new ChunkPos(chunkPos));
                    hasUpdates = true;
                }
            }
            pendingTicketRemoves.clear();
        }

        // clean up futures
        this.chunkLoadFutures.removeIf(CompletableFuture::isDone);

        // add new tickets
        while (this.chunkLoadFutures.size() < Config.maxConcurrentChunkLoads) {
            final ChunkPos pos = this.pendingTicketAdds.dequeue();
            if (pos == null) break;
            if (this.managedChunkTickets.add(pos.toLong())) {
                final CompletableFuture<Void> ticketFuture = this.addTicket0(pos);
                this.chunkLoadFutures.add(getChunkLoadFuture(tacs, pos, ticketFuture));
                hasUpdates = true;
            }
        }

        this.pendingTicketUpdatesCount = this.pendingTicketAdds.size() + this.pendingTicketRemoves.size();
        return hasUpdates;
    }

    private void removeTicket0(ChunkPos pos) {
        this.noTickSystem.mainBeforeTicketTicks.add(() -> this.chunkTicketManager.removeTicketWithLevel(TICKET_TYPE, pos, 33, pos));
    }

    private CompletableFuture<Void> addTicket0(ChunkPos pos) {
        return CompletableFuture.runAsync(() -> this.chunkTicketManager.addTicketWithLevel(TICKET_TYPE, pos, 33, pos), this.noTickSystem.mainBeforeTicketTicks::add);
    }

    private CompletableFuture<Void> getChunkLoadFuture(ThreadedAnvilChunkStorage tacs, ChunkPos pos, CompletableFuture<Void> ticketFuture) {
        final CompletableFuture<Void> future = ticketFuture.thenComposeAsync(unused -> {
            final ChunkHolder holder = ((IThreadedAnvilChunkStorage) tacs).getCurrentChunkHolders().get(pos.toLong());
            if (holder == null) {
                return CompletableFuture.completedFuture(null);
            } else {
                return holder.getAccessibleFuture().exceptionally(unused1 -> null).thenAccept(unused1 -> {
                });
            }
        }, this.noTickSystem.mainAfterTicketTicks::add);
        future.thenRunAsync(() -> {
            this.chunkLoadFutures.remove(future);
            final boolean hasUpdates = this.runPendingTicketUpdatesInternal(tacs);
            if (hasUpdates) {
                hasPendingTicketUpdatesAsync = true;
            }
        }, this.noTickSystem.executor);
        return future;
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = MathHelper.clamp(viewDistance, 3, MAX_RENDER_DISTANCE);
        sourceChunks.forEach((long value) -> {
            removeSource(new ChunkPos(value));
            addSource(new ChunkPos(value));
        });
    }

    public int getPendingTicketUpdatesCount() {
        return this.pendingTicketUpdatesCount;
    }

    public LongSet getChunks() {
        return managedChunkTickets;
    }

}
