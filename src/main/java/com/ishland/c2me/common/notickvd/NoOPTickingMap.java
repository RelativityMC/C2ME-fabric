package com.ishland.c2me.common.notickvd;

import net.minecraft.class_6609;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.math.ChunkPos;

import java.util.function.LongPredicate;

public class NoOPTickingMap extends class_6609 {

    @Override
    public void method_38637(long l, ChunkTicket<?> chunkTicket) {
    }

    @Override
    public void method_38641(long l, ChunkTicket<?> chunkTicket) {
    }

    @Override
    public <T> void method_38638(ChunkTicketType<T> chunkTicketType, ChunkPos chunkPos, int i, T object) {
    }

    @Override
    public <T> void method_38642(ChunkTicketType<T> chunkTicketType, ChunkPos chunkPos, int i, T object) {
    }

    @Override
    public void method_38636(int i) {
    }

    @Override
    protected int getInitialLevel(long id) {
        return super.getInitialLevel(id);
    }

    @Override
    public int method_38640(ChunkPos chunkPos) {
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
    public void method_38635() {
    }

    @Override
    public String method_38643(long l) {
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
