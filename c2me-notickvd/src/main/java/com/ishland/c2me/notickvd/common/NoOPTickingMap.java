package com.ishland.c2me.notickvd.common;

import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.SimulationDistanceLevelPropagator;

import java.util.function.LongPredicate;

public class NoOPTickingMap extends SimulationDistanceLevelPropagator {

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
        return 0;
    }

    @Override
    protected int getLevel(long id) {
        return 0;
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
