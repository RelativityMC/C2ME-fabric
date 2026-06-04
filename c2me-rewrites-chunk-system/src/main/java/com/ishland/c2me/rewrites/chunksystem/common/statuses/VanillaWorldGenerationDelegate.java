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

import com.ishland.c2me.base.common.scheduler.LockTokenImpl;
import com.ishland.c2me.base.common.scheduler.ScheduledTask;
import com.ishland.c2me.base.common.scheduler.SchedulingManager;
import com.ishland.c2me.base.common.threadstate.ThreadInstrumentation;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.c2me.rewrites.chunksystem.common.threadstate.ChunkTaskWork;
import com.ishland.flowsched.executor.LockToken;
import com.ishland.flowsched.scheduler.Cancellable;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import io.reactivex.rxjava3.core.Completable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkGenerationSteps;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.GenerationDependencies;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class VanillaWorldGenerationDelegate extends NewChunkStatus {

    private static final Logger LOGGER = LoggerFactory.getLogger("VanillaWorldGenerationDelegate");

    private static KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependencyFromStep(ChunkGenerationStep step) {
        ArrayList<KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>> deps = new ArrayList<>();
        final GenerationDependencies directDependencies = step.directDependencies();
        for (int x = -directDependencies.getMaxLevel(); x <= directDependencies.getMaxLevel(); x++) {
            for (int z = -directDependencies.getMaxLevel(); z <= directDependencies.getMaxLevel(); z++) {
                if (x == 0 && z == 0) continue;
                final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext> dep =
                        new KeyStatusPair<>(
                                new ChunkPos(x, z), fromVanillaStatus(directDependencies.get(Math.max(Math.abs(x), Math.abs(z))))
                        );
                deps.add(dep);
            }
        }

        return deps.toArray(KeyStatusPair[]::new);
    }

    public static <T> CompletableFuture<T> runTaskWithLockRadius(ChunkPos target, int radius, SchedulingManager schedulingManager, Supplier<CompletableFuture<T>> action) {
        return runTaskWithLockArea(target.x() - radius, target.z() - radius, radius * 2 + 1, radius * 2 + 1, schedulingManager, action);
    }

    public static <T> CompletableFuture<T> runTaskWithLockArea(int baseChunkX, int baseChunkZ, int sizeX, int sizeZ, SchedulingManager schedulingManager, Supplier<CompletableFuture<T>> action) {
        ObjectArrayList<LockToken> lockTargets = new ObjectArrayList<>(sizeX * sizeZ + 1);
        for (int dx = 0; dx < sizeX; dx++)
            for (int dz = 0; dz < sizeZ; dz++)
                lockTargets.add(new LockTokenImpl(schedulingManager.getId(), ChunkPos.toLong(baseChunkX + dx, baseChunkZ + dz), LockTokenImpl.Usage.WORLDGEN));

        final ScheduledTask<T> task = new ScheduledTask<>(
                ChunkPos.toLong(baseChunkX, baseChunkZ),
                action,
                lockTargets.toArray(LockToken[]::new));
        schedulingManager.enqueue(task);
        return task.getFuture();
    }

    private final ChunkStatus status;
    private final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] genDeps;
    private final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] loadDeps;
    private final int genDepsRadius;
    private final int loadDepsRadius;
    @Nullable
    private final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] toRemove;
    @Nullable
    private final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] toAdd;

    public VanillaWorldGenerationDelegate(int ordinal, ChunkStatus status) {
        super(ordinal, status);
        this.status = status;
        final ChunkGenerationStep genStep = ChunkGenerationSteps.GENERATION.get(status);
        final ChunkGenerationStep loadStep = ChunkGenerationSteps.LOADING.get(status);
        this.genDeps = getDependencyFromStep(genStep);
        this.loadDeps = getDependencyFromStep(loadStep);
        this.genDepsRadius = genStep.directDependencies().getMaxLevel();
        this.loadDepsRadius = loadStep.directDependencies().getMaxLevel();

        if (this.genDeps.length != this.loadDeps.length) {
            ObjectOpenHashSet<KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>> toRemove = new ObjectOpenHashSet<>(genDeps);
            toRemove.removeAll(List.of(loadDeps));
            this.toRemove = toRemove.toArray(KeyStatusPair[]::new);

            ObjectOpenHashSet<KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>> toAdd = new ObjectOpenHashSet<>(loadDeps);
            toAdd.removeAll(List.of(genDeps));
            this.toAdd = toAdd.toArray(KeyStatusPair[]::new);
        } else {
            if (Arrays.equals(this.genDeps, this.loadDeps)) {
                this.toRemove = EMPTY_DEPENDENCIES;
                this.toAdd = EMPTY_DEPENDENCIES;
            } else {
                LOGGER.warn("VanillaWorldGenerationDelegate with status {} has the same dependencies length for generation and loading", status);
                this.toRemove = null;
                this.toAdd = null;
            }
        }
    }

    @Override
    public Completable upgradeToThis(ChunkLoadingContext context, Cancellable cancellable) {
//        if (context.holder().getKey().equals(new ChunkPos(100, 100)) && this.status == ChunkStatus.FEATURES) {
//            throw new RuntimeException("boom");
//        }
        final ChunkState state = context.holder().getItem().get();
        if (state.reachedStatus().isAtLeast(this.status)) {
            return Completable.complete();
        }
        final ChunkGenerationContext chunkGenerationContext = ((IThreadedAnvilChunkStorage) context.tacs()).getGenerationContext();
        Chunk chunk = state.chunk();
        if (chunk.getStatus().isAtLeast(status)) {
            BoundedRegionArray<AbstractChunkHolder> boundedRegionArray = BoundedRegionArray.create(chunk.getPos().x(), chunk.getPos().z(), this.loadDepsRadius, (x, z) -> context.theChunkSystem().getHolder(new ChunkPos(x, z)).getUserData().get());
            try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, true))) {
                return Completable.defer(() -> Completable.fromCompletionStage(
                        ChunkGenerationSteps.LOADING.get(status)
                                .run(((IThreadedAnvilChunkStorage) context.tacs()).getGenerationContext(), boundedRegionArray, chunk)
                                .whenComplete((chunk1, throwable) -> {
                                    if (chunk1 != null) {
                                        context.holder().getItem().set(new ChunkState(chunk1, (ProtoChunk) chunk1, this.status, chunk1 instanceof WrapperProtoChunk));
                                    }
                                })
                ));
            }
        } else {
            final ChunkGenerationStep step = ChunkGenerationSteps.GENERATION.get(status);

            BoundedRegionArray<AbstractChunkHolder> boundedRegionArray = BoundedRegionArray.create(chunk.getPos().x(), chunk.getPos().z(), this.genDepsRadius, (x, z) -> context.theChunkSystem().getHolder(new ChunkPos(x, z)).getUserData().get());

            int radius = Math.max(0, step.blockStateWriteRadius());
            return Completable.defer(() -> Completable.fromCompletionStage(runTaskWithLockRadius(chunk.getPos(), radius, context.schedulingManager(),
                    () -> {
                        try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, true))) {
                            return step.run(chunkGenerationContext, boundedRegionArray, chunk)
                                    .whenComplete((chunk1, throwable) -> {
                                        if (chunk1 != null) {
                                            context.holder().getItem().set(new ChunkState(chunk1, (ProtoChunk) chunk1, this.status, chunk1 instanceof WrapperProtoChunk));
                                        }
                                    }).thenAccept(__ -> {
                                    });
                        }
                    }
            )));
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
        return Completable.complete();
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependencies(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        final Chunk chunk = holder.getItem().get().chunk();
        if (chunk == null) return genDeps;
        if (chunk.getStatus().isAtLeast(status)) {
            return relativeToAbsoluteDependencies(holder, loadDeps);
        } else {
            return relativeToAbsoluteDependencies(holder, genDeps);
        }
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToRemove(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        if (this.toRemove == null) return super.getDependenciesToRemove(holder);
        int curDepLen = getCurDepLen(holder);
        if (curDepLen == this.loadDeps.length) return EMPTY_DEPENDENCIES;
        if (curDepLen == this.genDeps.length) {
            final Chunk chunk = holder.getItem().get().chunk();
            if (chunk == null) return EMPTY_DEPENDENCIES;
            if (!chunk.getStatus().isAtLeast(status)) return EMPTY_DEPENDENCIES;
            return relativeToAbsoluteDependencies(holder, toRemove);
        }
        LOGGER.warn("Suspicious dependencies length for VanillaWorldGenerationDelegate with status {} on holder {}", this.status, holder.getKey());
        return super.getDependenciesToRemove(holder);
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToAdd(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        if (this.toAdd == null) return super.getDependenciesToAdd(holder);
        int curDepLen = getCurDepLen(holder);
        if (curDepLen == this.loadDeps.length) return EMPTY_DEPENDENCIES;
        if (curDepLen == this.genDeps.length) {
            final Chunk chunk = holder.getItem().get().chunk();
            if (chunk == null) return EMPTY_DEPENDENCIES;
            if (!chunk.getStatus().isAtLeast(status)) return EMPTY_DEPENDENCIES;
            return relativeToAbsoluteDependencies(holder, toAdd);
        }
        LOGGER.warn("Suspicious dependencies length for VanillaWorldGenerationDelegate with status {} on holder {}", this.status, holder.getKey());
        return super.getDependenciesToAdd(holder);
    }

    private int getCurDepLen(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] curDep = holder.getDependencies(this);
        return curDep.length;
    }

    @Override
    public String toString() {
        return this.status.toString();
    }
}
