package com.ishland.c2me.notickvd.common;

import com.ishland.c2me.base.mixin.access.IChunkTicket;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.util.collection.SortedArraySet;
import net.minecraft.world.ChunkPosDistanceLevelPropagator;

public class NormalTicketDistanceMap extends ChunkPosDistanceLevelPropagator {
    private final ChunkTicketManager chunkTicketManager;
    private final Long2IntOpenHashMap distanceMap = new Long2IntOpenHashMap();
    private final Long2ObjectOpenHashMap<SortedArraySet<ChunkTicket<?>>> ticketsByPosition = new Long2ObjectOpenHashMap<>();

    public NormalTicketDistanceMap(ChunkTicketManager chunkTicketManager) {
        super(33 + 2, 16, 256);
        this.chunkTicketManager = chunkTicketManager;
        distanceMap.defaultReturnValue(33 + 2);
    }

    @Override
    protected int getInitialLevel(long id) {
        SortedArraySet<ChunkTicket<?>> sortedArraySet = ((com.ishland.c2me.base.mixin.access.IChunkTicketManager) chunkTicketManager).getTicketsByPosition().get(id);
        if (sortedArraySet != null) {
            if (sortedArraySet.isEmpty()) return Integer.MAX_VALUE;
            for (ChunkTicket<?> next : sortedArraySet) {
                if (next.getType() == PlayerNoTickDistanceMap.TICKET_TYPE) continue;
                return next.getLevel();
            }
        }
        return Integer.MAX_VALUE;
    }

    @Override
    protected int getLevel(long id) {
        return distanceMap.get(id);
    }

    @Override
    protected void setLevel(long id, int level) {
        if (level > 33) {
            distanceMap.remove(id);
        } else {
            distanceMap.put(id, level);
        }
    }

    private static int getLevel(SortedArraySet<ChunkTicket<?>> sortedArraySet) {
        return !sortedArraySet.isEmpty() ? sortedArraySet.first().getLevel() : Integer.MAX_VALUE;
    }

    public void addTicket(long position, ChunkTicket<?> ticket) {
        if (ticket.getType() == PlayerNoTickDistanceMap.TICKET_TYPE) return;
        SortedArraySet<ChunkTicket<?>> sortedArraySet = this.getTicketSet(position);
        int i = getLevel(sortedArraySet);
        sortedArraySet.add(ticket);
        if (ticket.getLevel() < i) {
            this.updateLevel(position, ticket.getLevel(), true);
        }
    }

    public void removeTicket(long pos, ChunkTicket<?> ticket) {
        if (ticket.getType() == PlayerNoTickDistanceMap.TICKET_TYPE) return;
        SortedArraySet<ChunkTicket<?>> sortedArraySet = this.getTicketSet(pos);
        sortedArraySet.remove(ticket);

        if (sortedArraySet.isEmpty()) {
            this.ticketsByPosition.remove(pos);
        }

        this.updateLevel(pos, getLevel(sortedArraySet), false);
    }

    public void purge(long age) {
        final ObjectIterator<Long2ObjectMap.Entry<SortedArraySet<ChunkTicket<?>>>> iterator = this.ticketsByPosition.long2ObjectEntrySet().fastIterator();

        while (iterator.hasNext()) {
            final Long2ObjectMap.Entry<SortedArraySet<ChunkTicket<?>>> entry = iterator.next();
            final boolean isModified = entry.getValue().removeIf(chunkTicket -> ((IChunkTicket) (Object) chunkTicket).invokeIsExpired(age));
            if (isModified) {
                this.updateLevel(entry.getLongKey(), getLevel(entry.getValue()), false);
            }
            if (entry.getValue().isEmpty()) {
                iterator.remove();
            }
        }
    }

    public SortedArraySet<ChunkTicket<?>> getTicketSet(long pos) {
        return this.ticketsByPosition.computeIfAbsent(pos, (l) -> SortedArraySet.create(4));
    }

    public boolean update() {
        return Integer.MAX_VALUE - this.applyPendingUpdates(Integer.MAX_VALUE) != 0;
    }

    public LongSet getChunks() {
        return this.distanceMap.keySet();
    }
}
