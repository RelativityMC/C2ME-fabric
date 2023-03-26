package com.ishland.c2me.notickvd.common;

import com.ishland.c2me.base.common.GlobalExecutors;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import org.threadly.concurrent.NoThreadScheduler;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class NoTickSystem {

    private final PlayerNoTickDistanceMap playerNoTickDistanceMap;
    private final NormalTicketDistanceMap normalTicketDistanceMap;
    private final ChunkTicketManager chunkTicketManager;

    private final ConcurrentLinkedQueue<Runnable> pendingActions = new ConcurrentLinkedQueue<>();

    final NoThreadScheduler noThreadScheduler = new NoThreadScheduler();

    private final AtomicBoolean isTicking = new AtomicBoolean();
    final ExecutorService executor = GlobalExecutors.asyncScheduler;
    private volatile LongSet noTickOnlyChunksSnapshot = LongSets.EMPTY_SET;
    private volatile boolean pendingPurge = false;
    private volatile long age = 0;

    public NoTickSystem(ChunkTicketManager chunkTicketManager) {
        this.chunkTicketManager = chunkTicketManager;
        this.playerNoTickDistanceMap = new PlayerNoTickDistanceMap(chunkTicketManager, this);
        this.normalTicketDistanceMap = new NormalTicketDistanceMap(chunkTicketManager);
    }

    public void onTicketAdded(long position, ChunkTicket<?> ticket) {
        this.pendingActions.add(() -> this.normalTicketDistanceMap.addTicket(position, ticket));
    }

    public void onTicketRemoved(long position, ChunkTicket<?> ticket) {
        this.pendingActions.add(() -> this.normalTicketDistanceMap.removeTicket(position, ticket));
    }

    public void addPlayerSource(ChunkPos chunkPos) {
        this.pendingActions.add(() -> this.playerNoTickDistanceMap.addSource(chunkPos));
    }

    public void removePlayerSource(ChunkPos chunkPos) {
        this.pendingActions.add(() -> this.playerNoTickDistanceMap.removeSource(chunkPos));
    }

    public void setNoTickViewDistance(int viewDistance) {
        this.pendingActions.add(() -> this.playerNoTickDistanceMap.setViewDistance(viewDistance));
    }

    public void tickScheduler() {
        this.noThreadScheduler.tick(Throwable::printStackTrace);
    }

    public void tick(ThreadedAnvilChunkStorage tacs) {
        tickScheduler();
        scheduleTick(tacs);
    }

    private void scheduleTick(ThreadedAnvilChunkStorage tacs) {
        if (this.isTicking.compareAndSet(false, true))
            executor.execute(() -> {
                Runnable runnable;
                while ((runnable = this.pendingActions.poll()) != null) {
                    try {
                        runnable.run();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }

                boolean hasNoTickTicketUpdates;
                if (pendingPurge) {
                    this.normalTicketDistanceMap.purge(this.age);
                    hasNoTickTicketUpdates = this.playerNoTickDistanceMap.runPendingTicketUpdates(tacs);
                } else {
                    hasNoTickTicketUpdates = false;
                }

                final boolean hasNormalTicketUpdates = this.normalTicketDistanceMap.update();
                final boolean hasNoTickUpdates = this.playerNoTickDistanceMap.update();
                if (hasNormalTicketUpdates || hasNoTickUpdates || hasNoTickTicketUpdates) {
                    final LongSet noTickChunks = this.playerNoTickDistanceMap.getChunks();
                    final LongSet normalChunks = this.normalTicketDistanceMap.getChunks();
                    final LongOpenHashSet longs = new LongOpenHashSet(noTickChunks.size() * 3 / 2);
                    final LongIterator iterator = noTickChunks.iterator();
                    while (iterator.hasNext()) {
                        final long chunk = iterator.nextLong();
                        if (normalChunks.contains(chunk)) continue;
                        longs.add(chunk);
                    }
                    this.noTickOnlyChunksSnapshot = LongSets.unmodifiable(longs);
                }
                this.isTicking.set(false);
                if (hasNormalTicketUpdates || hasNoTickUpdates) scheduleTick(tacs); // run more tasks
            });
    }

    public void runPurge(long age) {
        this.age = age;
        this.pendingPurge = true;
    }

    public LongSet getNoTickOnlyChunksSnapshot() {
        return this.noTickOnlyChunksSnapshot;
    }

    public int getPendingNoTickTicketUpdatesCount() {
        return this.playerNoTickDistanceMap.getPendingTicketUpdatesCount();
    }
}
