package com.ishland.c2me.common.notickvd;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.mixin.access.IChunkTicketManager;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkPosDistanceLevelPropagator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class PlayerNoTickDistanceMap extends ChunkPosDistanceLevelPropagator {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final ChunkTicketType<ChunkPos> TICKET_TYPE = ChunkTicketType.create("c2me_no_tick_vd", Comparator.comparingLong(ChunkPos::toLong));

    private static final int MAX_TICKET_UPDATES_PER_TICK = C2MEConfig.noTickViewDistanceConfig.updatesPerTick;

    private final LongSet sourceChunks = new LongOpenHashSet();
    private final Long2IntOpenHashMap distanceFromNearestPlayer = new Long2IntOpenHashMap();
    private final TreeMap<Long, Boolean> pendingTicketUpdates = new TreeMap<>();
    private final LongOpenHashSet managedChunkTickets = new LongOpenHashSet();

    private final ChunkTicketManager chunkTicketManager;
    private final NoTickSystem noTickSystem;
    private volatile int viewDistance;
    private volatile int pendingTicketUpdatesCount = 0; // for easier access concurrently

    public PlayerNoTickDistanceMap(ChunkTicketManager chunkTicketManager, NoTickSystem noTickSystem) {
        super(251, 16, 256);
        this.chunkTicketManager = chunkTicketManager;
        this.noTickSystem = noTickSystem;
        this.distanceFromNearestPlayer.defaultReturnValue(251);
        this.setViewDistance(12);
    }

    @Override
    protected int getInitialLevel(long chunkPos) {
        final ObjectSet<ServerPlayerEntity> players = ((IChunkTicketManager) chunkTicketManager).getPlayersByChunkPos().get(chunkPos);
        return players != null && !players.isEmpty() ? 249 - viewDistance : Integer.MAX_VALUE;
    }

    @Override
    protected int getLevel(long chunkPos) {
        return this.distanceFromNearestPlayer.get(chunkPos);
    }

    @Override
    protected void setLevel(long chunkPos, int level) {
        if (level > 249) {
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
        this.updateLevel(chunkPos.toLong(), 249 - this.viewDistance, true);
        this.sourceChunks.add(chunkPos.toLong());
    }

    public void removeSource(ChunkPos chunkPos) {
        this.updateLevel(chunkPos.toLong(), Integer.MAX_VALUE, false);
        this.sourceChunks.remove(chunkPos.toLong());
    }

    public boolean update() {
        final int pendingRawUpdateCount = this.getPendingUpdateCount();
        if (pendingRawUpdateCount == 0) return false;
        this.applyPendingUpdates(Integer.MAX_VALUE);
        this.pendingTicketUpdatesCount = this.pendingTicketUpdates.size();
        return true;
    }

    void runPendingTicketUpdates() {
        final Iterator<Map.Entry<Long, Boolean>> iterator = this.pendingTicketUpdates.entrySet().iterator();
        int i = 0;
        while (iterator.hasNext() && i <= MAX_TICKET_UPDATES_PER_TICK) {
            final Map.Entry<Long, Boolean> entry = iterator.next();
            final long chunkPos = entry.getKey();
            ChunkPos pos = new ChunkPos(chunkPos);
            if (entry.getValue()) {
                if (this.managedChunkTickets.add(chunkPos)) {
                    this.noTickSystem.noThreadScheduler.execute(() -> this.chunkTicketManager.addTicketWithLevel(TICKET_TYPE, pos, 33, pos));
                    i ++;
                }
            } else {
                if (this.managedChunkTickets.remove(chunkPos)) {
                    this.noTickSystem.noThreadScheduler.execute(() -> this.chunkTicketManager.removeTicketWithLevel(TICKET_TYPE, pos, 33, pos));
                    i ++;
                }
            }
            iterator.remove();
        }
        this.pendingTicketUpdatesCount = this.pendingTicketUpdates.size();
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = MathHelper.clamp(viewDistance, 3, 249);
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
