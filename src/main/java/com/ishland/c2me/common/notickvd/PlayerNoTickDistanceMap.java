package com.ishland.c2me.common.notickvd;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.mixin.access.IChunkTicketManager;
import com.ishland.c2me.mixin.access.IThreadedAnvilChunkStorage;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkPosDistanceLevelPropagator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

public class PlayerNoTickDistanceMap extends ChunkPosDistanceLevelPropagator {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final ChunkTicketType<ChunkPos> TICKET_TYPE = ChunkTicketType.create("c2me_no_tick_vd", Comparator.comparingLong(ChunkPos::toLong));

    private static final int MAX_TICKET_UPDATES_PER_TICK = C2MEConfig.noTickViewDistanceConfig.updatesPerTick;

    private final Long2IntOpenHashMap distanceFromNearestPlayer = new Long2IntOpenHashMap();
    private final TreeMap<Long, Boolean> pendingTicketUpdates = new TreeMap<>();
    private final LongOpenHashSet managedChunkTickets = new LongOpenHashSet();

    private final ChunkTicketManager chunkTicketManager;
    private final int maxDistance;

    private final AtomicLong lastTickNumber = new AtomicLong(0L);

    public PlayerNoTickDistanceMap(ChunkTicketManager chunkTicketManager, int maxDistance) {
        super(maxDistance + 2, 16, 256);
        this.chunkTicketManager = chunkTicketManager;
        this.maxDistance = maxDistance;
        this.distanceFromNearestPlayer.defaultReturnValue(maxDistance + 2);
    }

    @Override
    protected int getInitialLevel(long chunkPos) {
        final ObjectSet<ServerPlayerEntity> players = ((IChunkTicketManager) chunkTicketManager).getPlayersByChunkPos().get(chunkPos);
        return players != null && !players.isEmpty() ? 0 : Integer.MAX_VALUE;
    }

    @Override
    protected int getLevel(long chunkPos) {
        return this.distanceFromNearestPlayer.get(chunkPos);
    }

    @Override
    protected void setLevel(long chunkPos, int level) {
        if (level > this.maxDistance) {
            if (this.distanceFromNearestPlayer.containsKey(chunkPos)) {
                pendingTicketUpdates.put(chunkPos, false);
                this.distanceFromNearestPlayer.remove(chunkPos);
            }
        } else {
            if (!this.distanceFromNearestPlayer.containsKey(chunkPos)) {
                pendingTicketUpdates.put(chunkPos, true);
            }
            this.distanceFromNearestPlayer.put(chunkPos, level);
        }
    }

    public void addSource(ChunkPos chunkPos) {
        this.updateLevel(chunkPos.toLong(), 0, true);
    }

    public void removeSource(ChunkPos chunkPos) {
        this.updateLevel(chunkPos.toLong(), Integer.MAX_VALUE, false);
    }

    public void update(ThreadedAnvilChunkStorage threadedAnvilChunkStorage) {
        if (((IThreadedAnvilChunkStorage) threadedAnvilChunkStorage).getWorld().getServer().getTicks() == lastTickNumber.get()) return;
        lastTickNumber.addAndGet(1);
        this.runPendingTicketUpdates();
        final int pendingRawUpdateCount = this.getPendingUpdateCount();
        if (pendingRawUpdateCount == 0) return;
        this.applyPendingUpdates(Integer.MAX_VALUE);
    }

    private void runPendingTicketUpdates() {
        final Iterator<Map.Entry<Long, Boolean>> iterator = this.pendingTicketUpdates.entrySet().iterator();
        int i = 0;
        while (iterator.hasNext() && i <= MAX_TICKET_UPDATES_PER_TICK) {
            final Map.Entry<Long, Boolean> entry = iterator.next();
            final long chunkPos = entry.getKey();
            ChunkPos pos = new ChunkPos(chunkPos);
            if (entry.getValue()) {
                if (this.managedChunkTickets.add(chunkPos)) {
                    this.chunkTicketManager.addTicketWithLevel(TICKET_TYPE, pos, 33, pos);
                    i ++;
                }
            } else {
                if (this.managedChunkTickets.remove(chunkPos)) {
                    this.chunkTicketManager.removeTicketWithLevel(TICKET_TYPE, pos, 33, pos);
                    i ++;
                }
            }
            iterator.remove();
        }
    }

    public int getPendingTicketUpdatesCount() {
        return this.pendingTicketUpdates.size();
    }

    public LongSet getChunks() {
        return managedChunkTickets;
    }

}
