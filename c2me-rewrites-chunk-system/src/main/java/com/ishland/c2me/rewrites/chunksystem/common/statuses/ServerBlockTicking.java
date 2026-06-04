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

package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.google.common.base.Suppliers;
import com.ishland.c2me.base.common.config.ModStatuses;
import com.ishland.c2me.base.common.threadstate.ThreadInstrumentation;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.Config;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkHolderVanillaInterface;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.c2me.rewrites.chunksystem.common.ducks.WorldChunkExtension;
import com.ishland.c2me.rewrites.chunksystem.common.fapi.LifecycleEventInvoker;
import com.ishland.c2me.rewrites.chunksystem.common.quirks.FlowableFluidUtils;
import com.ishland.c2me.rewrites.chunksystem.common.threadstate.ChunkTaskWork;
import com.ishland.flowsched.scheduler.Cancellable;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import com.ishland.flowsched.util.Assertions;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.ChunkGenerationSteps;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ServerBlockTicking extends NewChunkStatus {

    private static final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] deps;

    static {
        deps = new KeyStatusPair[] {
                new KeyStatusPair<>(new ChunkPos(-1, -1), NewChunkStatus.SERVER_ACCESSIBLE),
                new KeyStatusPair<>(new ChunkPos(-1, 0), NewChunkStatus.SERVER_ACCESSIBLE),
                new KeyStatusPair<>(new ChunkPos(-1, 1), NewChunkStatus.SERVER_ACCESSIBLE),
                new KeyStatusPair<>(new ChunkPos(0, -1), NewChunkStatus.SERVER_ACCESSIBLE),
                new KeyStatusPair<>(new ChunkPos(0, 1), NewChunkStatus.SERVER_ACCESSIBLE),
                new KeyStatusPair<>(new ChunkPos(1, -1), NewChunkStatus.SERVER_ACCESSIBLE),
                new KeyStatusPair<>(new ChunkPos(1, 0), NewChunkStatus.SERVER_ACCESSIBLE),
                new KeyStatusPair<>(new ChunkPos(1, 1), NewChunkStatus.SERVER_ACCESSIBLE),
        };
    }

    public ServerBlockTicking(int ordinal) {
        super(ordinal, ChunkStatus.FULL);
    }

    @Override
    public Completable upgradeToThis(ChunkLoadingContext context, Cancellable cancellable) {
        if (Config.filterFluidPostProcessing) {
            try {
                filterFluidTicks(context);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return Completable
                .fromRunnable(() -> {
                    Assertions.assertTrue(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor().isOnThread());

                    try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, true))) {
                        final WorldChunk chunk = (WorldChunk) context.holder().getItem().get().chunk();
                        ServerWorld serverWorld = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
                        chunk.runPostProcessing(serverWorld);
                        serverWorld.disableTickSchedulers(chunk);
                        sendChunkToPlayer(context);
                        ((WorldChunkExtension) chunk).c2me$setBlockTicking(true);
                        if (ModStatuses.fabric_lifecycle_events_v1) {
                            LifecycleEventInvoker.invokeChunkLevelTypeChange(serverWorld, chunk, ChunkLevelType.FULL, ChunkLevelType.BLOCK_TICKING);
                        }
                    }
                })
                .subscribeOn(Schedulers.from(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()));
    }

    private static void filterFluidTicks(ChunkLoadingContext context) {
        final WorldChunk chunk = (WorldChunk) context.holder().getItem().get().chunk();
        ChunkPos pos = context.holder().getKey();

        BoundedRegionArray<AbstractChunkHolder> boundedRegionArray = BoundedRegionArray.create(pos.x(), pos.z(), 1, (x, z) -> context.theChunkSystem().getHolder(new ChunkPos(x, z)).getUserData().get());
        Supplier<ChunkRegion> chunkRegionSupplier = Suppliers.memoize(() -> new ChunkRegion(((IThreadedAnvilChunkStorage) context.tacs()).getWorld(), boundedRegionArray, ChunkGenerationSteps.GENERATION.get(ChunkStatus.FEATURES), chunk));

        int total = 0;
        int eliminated = 0;
        ShortList[] postProcessingLists = chunk.getPostProcessingLists();
        for (int i = 0; i < postProcessingLists.length; i++) {
            if (postProcessingLists[i] != null) {
                for (ShortListIterator iterator = postProcessingLists[i].iterator(); iterator.hasNext(); ) {
                    Short short_ = iterator.next();
                    BlockPos blockPos = ProtoChunk.joinBlockPos(short_, chunk.sectionIndexToCoord(i), chunk.getPos());
                    BlockState blockState = chunk.getBlockState(blockPos);
                    FluidState fluidState = blockState.getFluidState();
                    if (!fluidState.isEmpty() && fluidState.getFluid() instanceof FlowableFluid) {
                        total ++;
                        if (!FlowableFluidUtils.needsPostProcessing(chunkRegionSupplier.get(), blockPos, blockState, fluidState)) {
                            iterator.remove();
                            eliminated ++;
                        }
                    }
                }
            }
        }

//        if (total > 0) {
//            System.out.println(String.format("Eliminated %d/%d (%.2f%%) post processing fluids in chunk %s", eliminated, total, eliminated / (double) total * 100.0, context.holder().getKey()));
//        }
    }

    private static void sendChunkToPlayer(ChunkLoadingContext context) {
        final WorldChunk chunk = (WorldChunk) context.holder().getItem().get().chunk();
        NewChunkHolderVanillaInterface holderVanillaInterface = context.holder().getUserData().get();
        CompletableFuture<?> completableFuturexx = holderVanillaInterface.getPostProcessingFuture();
        if (completableFuturexx.isDone()) {
            ((IThreadedAnvilChunkStorage) context.tacs()).invokeSendToPlayers(holderVanillaInterface, chunk);
        } else {
            completableFuturexx.thenAcceptAsync(v -> ((IThreadedAnvilChunkStorage) context.tacs()).invokeSendToPlayers(holderVanillaInterface, chunk), ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor());
        }
    }

    @Override
    public Completable postUpgradeToThis(ChunkLoadingContext context) {
        return Completable.complete();
    }

    @Override
    public Completable preDowngradeFromThis(ChunkLoadingContext context, Cancellable cancellable) {
        return Completable.complete();
    }

    @Override
    public Completable downgradeFromThis(ChunkLoadingContext context, Cancellable cancellable) {
        ServerWorld serverWorld = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
        final WorldChunk chunk = (WorldChunk) context.holder().getItem().get().chunk();
        ((WorldChunkExtension) chunk).c2me$setBlockTicking(false);
        if (ModStatuses.fabric_lifecycle_events_v1 && LifecycleEventInvoker.needsInvokeChunkLevelTypeChange()) {
            return Completable
                    .fromRunnable(() -> {
                        Assertions.assertTrue(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor().isOnThread());

                        try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, false))) {
                            LifecycleEventInvoker.invokeChunkLevelTypeChange(serverWorld, chunk, ChunkLevelType.BLOCK_TICKING, ChunkLevelType.FULL);
                        }
                    })
                    .subscribeOn(Schedulers.from(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()));
        }
        return Completable.complete();
        // TODO check if syncing is needed
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependencies(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return relativeToAbsoluteDependencies(holder, deps);
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToRemove(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return EMPTY_DEPENDENCIES;
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToAdd(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return EMPTY_DEPENDENCIES;
    }

    @Override
    public String toString() {
        return "Block Ticking";
    }
}
