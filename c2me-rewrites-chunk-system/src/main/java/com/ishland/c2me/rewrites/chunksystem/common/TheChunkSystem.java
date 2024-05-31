package com.ishland.c2me.rewrites.chunksystem.common;

import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.flowsched.scheduler.DaemonizedStatusAdvancingScheduler;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.ItemStatus;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import com.ishland.flowsched.util.Assertions;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadFactory;

public class TheChunkSystem extends DaemonizedStatusAdvancingScheduler<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> {

    private final Long2IntMap managedTickets = Long2IntMaps.synchronize(new Long2IntOpenHashMap());
    private final ThreadedAnvilChunkStorage tacs;

    public TheChunkSystem(ThreadFactory threadFactory, ThreadedAnvilChunkStorage tacs) {
        super(threadFactory);
        this.tacs = tacs;
        managedTickets.defaultReturnValue(NewChunkStatus.vanillaLevelToStatus.length - 1);
    }

    @Override
    protected ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext> getUnloadedStatus() {
        return NewChunkStatus.NEW;
    }

    @Override
    protected ChunkLoadingContext makeContext(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder, ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext> nextStatus, KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] dependencies, boolean isUpgrade) {
        Assertions.assertTrue(nextStatus instanceof NewChunkStatus);
        final NewChunkStatus nextStatus1 = (NewChunkStatus) nextStatus;
        final List<Chunk> chunks = Arrays.stream(dependencies)
                .map(pair -> {
                    Chunk chunk = this.getHolder(pair.key()).getItem().get().chunk();
                    if (nextStatus1.getEffectiveVanillaStatus() != ChunkStatus.FULL && chunk instanceof WorldChunk worldChunk) {
                        chunk = new WrapperProtoChunk(worldChunk, false);
                    }
                    return chunk;
                }).toList();
        return new ChunkLoadingContext(holder, this.tacs, chunks);
    }

    @Override
    protected void onItemCreation(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder) {
        super.onItemCreation(holder);
        holder.getUserData().set(new NewChunkHolderVanillaInterface(holder, ((IThreadedAnvilChunkStorage) this.tacs).getWorld(), ((IThreadedAnvilChunkStorage) this.tacs).getLightingProvider(), this.tacs));
    }

    @Override
    protected void onItemRemoval(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder) {
        super.onItemRemoval(holder);
    }

    public ChunkHolder vanillaIf$setLevel(long pos, int level) {
        synchronized (this.managedTickets) {
            final int oldLevel = this.managedTickets.put(pos, level);
            NewChunkStatus oldStatus = NewChunkStatus.fromVanillaLevel(oldLevel);
            NewChunkStatus newStatus = NewChunkStatus.fromVanillaLevel(level);
            if (oldStatus != newStatus) {
                ChunkHolder vanillaHolder;
                if (newStatus != this.getUnloadedStatus()) {
                    final ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder = this.addTicket(new ChunkPos(pos), newStatus, () -> {
                    });
                    vanillaHolder = holder.getUserData().get();
                } else {
                    this.managedTickets.remove(pos);
                    vanillaHolder = null;
                }
                if (oldStatus != this.getUnloadedStatus()) {
                    this.removeTicket(new ChunkPos(pos), oldStatus);
                }
                return vanillaHolder;
            } else {
                return null;
            }
        }
    }
}
