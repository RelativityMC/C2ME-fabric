package com.ishland.c2me.notickvd.common;

import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.SimulationDistanceLevelPropagator;

import java.util.function.LongPredicate;

public class NoOPTickingMap extends SimulationDistanceLevelPropagator {

    private ServerChunkLoadingManager tacs = null;

    public void setTACS(ServerChunkLoadingManager tacs) {
        this.tacs = tacs;
    }

    @Override
    public void add(long l, ChunkTicket<?> chunkTicket) {
    }

    @Override
    public void remove(long l, ChunkTicket<?> chunkTicket) {
    }

    @Override
    public <T> void add(ChunkTicketType<T> chunkTicketType, ChunkPos chunkPos, int i, T object) {
    }

    @Override
    public <T> void remove(ChunkTicketType<T> chunkTicketType, ChunkPos chunkPos, int i, T object) {
    }

    @Override
    public void updatePlayerTickets(int i) {
    }

    @Override
    protected int getInitialLevel(long id) {
        return super.getInitialLevel(id);
    }

    @Override
    public int getLevel(ChunkPos chunkPos) {
        return getLevel(chunkPos.toLong());
    }

    @Override
    protected int getLevel(long id) {
        if (tacs != null) {
            final ChunkHolder holder = ((IThreadedAnvilChunkStorage) tacs).getCurrentChunkHolders().get(id);
            return holder != null ? holder.getLevel() : ChunkLevels.INACCESSIBLE + 1;
        } else {
            return 0;
        }
    }

    @Override
    protected void setLevel(long id, int level) {
    }

    @Override
    public void updateLevels() {
    }

    @Override
    public String getTickingTicket(long l) {
        return "no-op";
    }

    @Override
    protected boolean isMarker(long id) {
        return super.isMarker(id);
    }

    @Override
    protected void propagateLevel(long id, int level, boolean decrease) {
        super.propagateLevel(id, level, decrease);
    }

    @Override
    protected int recalculateLevel(long id, long excludedId, int maxLevel) {
        return super.recalculateLevel(id, excludedId, maxLevel);
    }

    @Override
    protected int getPropagatedLevel(long sourceId, long targetId, int level) {
        return super.getPropagatedLevel(sourceId, targetId, level);
    }

    @Override
    public void updateLevel(long chunkPos, int distance, boolean decrease) {
    }

    @Override
    protected void removePendingUpdate(long id) {
    }

    @Override
    public void removePendingUpdateIf(LongPredicate predicate) {
    }

    @Override
    protected void resetLevel(long id) {
    }

    @Override
    protected void updateLevel(long sourceId, long id, int level, boolean decrease) {
    }

    @Override
    public int getPendingUpdateCount() {
        return 0;
    }
}
