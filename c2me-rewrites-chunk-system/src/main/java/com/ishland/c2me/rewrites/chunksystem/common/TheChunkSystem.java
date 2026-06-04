/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.rewrites.chunksystem.common;

import com.ishland.c2me.base.common.scheduler.IVanillaChunkManager;
import com.ishland.c2me.base.common.scheduler.SchedulingManager;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.base.mixin.access.IVersionedChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.structs.ChunkSystemExecutors;
import com.ishland.flowsched.scheduler.ExceptionHandlingAction;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.ItemStatus;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import com.ishland.flowsched.scheduler.StatusAdvancingScheduler;
import com.ishland.flowsched.util.Assertions;
import io.reactivex.rxjava3.core.Scheduler;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public class TheChunkSystem extends StatusAdvancingScheduler<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> {

    private final Logger LOGGER;

    private final Long2IntMap managedTickets = Long2IntMaps.synchronize(new Long2IntOpenHashMap());
    private final SchedulingManager schedulingManager;
    private final ServerChunkLoadingManager tacs;

    public TheChunkSystem(ServerChunkLoadingManager tacs) {
        super(TheSpeedyObjectFactory.INSTANCE);
        this.tacs = tacs;
        this.schedulingManager =  ((IVanillaChunkManager) tacs).c2me$getSchedulingManager();
        this.LOGGER = LoggerFactory.getLogger("Chunk System of %s".formatted(((IThreadedAnvilChunkStorage) tacs).getWorld().getRegistryKey().getValue()));
        managedTickets.defaultReturnValue(NewChunkStatus.vanillaLevelToStatus.length - 1);
    }

    @Override
    protected Executor getBackgroundExecutor() {
        return ChunkSystemExecutors.consolidatingBackgroundExecutor;
    }

    @Override
    protected Scheduler getSchedulerBackedByBackgroundExecutor() {
        return ChunkSystemExecutors.consolidatingBackgroundScheduler;
    }

    @Override
    protected ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext> getUnloadedStatus() {
        return NewChunkStatus.NEW;
    }

    @Override
    protected ChunkLoadingContext makeContext(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder, ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext> nextStatus, KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] dependencies, boolean isUpgrade) {
        Assertions.assertTrue(nextStatus instanceof NewChunkStatus);
        assert nextStatus != null;
        final NewChunkStatus nextStatus1 = (NewChunkStatus) nextStatus;

        return new ChunkLoadingContext(holder, this.tacs, this.schedulingManager, this, dependencies);
    }

    @Override
    protected ExceptionHandlingAction handleTransactionException(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder, ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext> nextStatus, boolean isUpgrade, Throwable throwable) {
        if (isUpgrade) {
            LOGGER.error("Error upgrading chunk {} to \"{}\"", holder.getKey(), nextStatus, throwable);
        } else {
            LOGGER.error("Error downgrading chunk {} to \"{}\"", holder.getKey(), nextStatus, throwable);
        }
        final MinecraftServer server = ((IThreadedAnvilChunkStorage) this.tacs).getWorld().getServer();
        server.execute(() -> server.onChunkLoadFailure(throwable, ((IVersionedChunkStorage) this.tacs).invokeGetStorageKey(), holder.getKey()));
        return ExceptionHandlingAction.MARK_BROKEN;
    }

    @Override
    protected void onItemConstruct(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder) {
        super.onItemConstruct(holder);
        holder.getUserData().setPlain(new NewChunkHolderVanillaInterface(this, holder, ((IThreadedAnvilChunkStorage) this.tacs).getWorld(), ((IThreadedAnvilChunkStorage) this.tacs).getLightingProvider(), this.tacs));
        holder.getItem().setPlain(new ChunkState(null, null, null, true));
    }

    @Override
    protected void onItemCreation(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder) {
        super.onItemCreation(holder);
    }

    @Override
    protected void onItemRemoval(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder) {
        super.onItemRemoval(holder);
    }

    @Override
    protected void onItemUpgrade(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder, ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext> statusReached) {
        super.onItemUpgrade(holder, statusReached);
        final NewChunkStatus statusReached1 = (NewChunkStatus) statusReached;
        final NewChunkStatus prevStatus = (NewChunkStatus) statusReached.getPrev();
        if (prevStatus.toChunkLevelType() != statusReached1.toChunkLevelType()) {
            ((IThreadedAnvilChunkStorage) this.tacs).getMainThreadExecutor().execute(
                    () -> ((IThreadedAnvilChunkStorage) this.tacs).invokeOnChunkStatusChange(holder.getKey(), statusReached1.toChunkLevelType()));
        }
    }

    @Override
    protected void onItemDowngrade(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder, ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext> statusReached) {
        super.onItemDowngrade(holder, statusReached);
        final NewChunkStatus statusReached1 = (NewChunkStatus) statusReached;
        final NewChunkStatus prevStatus = (NewChunkStatus) statusReached.getNext();
        if (prevStatus.toChunkLevelType() != statusReached1.toChunkLevelType()) {
            ((IThreadedAnvilChunkStorage) this.tacs).getMainThreadExecutor().execute(
                    () -> ((IThreadedAnvilChunkStorage) this.tacs).invokeOnChunkStatusChange(holder.getKey(), statusReached1.toChunkLevelType()));
        }
    }

    public ChunkHolder vanillaIf$setLevel(long pos, int level) {
        assert !Thread.holdsLock(this.managedTickets);
        synchronized (this.managedTickets) {
            final int oldLevel = this.managedTickets.put(pos, level);
            NewChunkStatus oldStatus = c2me$getDeferredStatusFromVanillaLevel(oldLevel);
            NewChunkStatus newStatus = c2me$getDeferredStatusFromVanillaLevel(level);
            final ChunkPos key = ChunkPos.fromLong(pos);
            if (oldStatus != newStatus) {
                NewChunkHolderVanillaInterface vanillaHolder;
                ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder;
                boolean shouldReturnVanillaHolder;
                if (newStatus != this.getUnloadedStatus()) {
                    holder = this.addTicket(key, TicketTypeExtension.VANILLA_LEVEL, key, newStatus, null);
                    shouldReturnVanillaHolder = true;
                } else {
                    this.managedTickets.remove(pos);
                    holder = this.getHolder(key);
                    shouldReturnVanillaHolder = false;
                }
                Assertions.assertTrue(holder != null, "Holder should be managed by the vanilla interface");
                assert holder != null;
                vanillaHolder = holder.getUserData().get();
                if (!Config.useLegacyScheduling) {
                    vanillaHolder.updateDeferredStatus(NewChunkStatus.fromVanillaLevel(level));
                }

                if (oldStatus != this.getUnloadedStatus()) {
                    this.removeTicket(key, TicketTypeExtension.VANILLA_LEVEL, key, oldStatus);
                }
                return shouldReturnVanillaHolder ? vanillaHolder : null;
            } else {
                final ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder = this.getHolder(key);
                NewChunkHolderVanillaInterface vanillaHolder;
                if (holder != null) {
                    vanillaHolder = holder.getUserData().get();

                } else {
                    vanillaHolder = null;
                }
                if (!Config.useLegacyScheduling && vanillaHolder != null) {
                    vanillaHolder.updateDeferredStatus(NewChunkStatus.fromVanillaLevel(level));
                }
                if (newStatus != this.getUnloadedStatus() && vanillaHolder != null) {
                    return vanillaHolder;
                }
                return null;
            }
        }
    }

    private static NewChunkStatus c2me$getDeferredStatusFromVanillaLevel(int level) {
        NewChunkStatus status = NewChunkStatus.fromVanillaLevel(level);
        if (!Config.useLegacyScheduling) {
            if (status == NewChunkStatus.NEW) {
                return status;
            } else if (status.ordinal() < NewChunkStatus.SERVER_ACCESSIBLE.ordinal()) {
                return NewChunkStatus.DEFERRED;
            } else {
                return status;
            }
        } else {
            return status;
        }
    }

    public int vanillaIf$getManagedLevel(long pos) {
        return this.managedTickets.get(pos);
    }
}
