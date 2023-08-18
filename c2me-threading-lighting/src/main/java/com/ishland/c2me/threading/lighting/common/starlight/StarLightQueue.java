package com.ishland.c2me.threading.lighting.common.starlight;

import ca.spottedleaf.starlight.common.light.BlockStarLightEngine;
import ca.spottedleaf.starlight.common.light.SkyStarLightEngine;
import ca.spottedleaf.starlight.common.light.StarLightInterface;
import ca.spottedleaf.starlight.common.util.CoordinateUtils;
import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.common.scheduler.IVanillaChunkManager;
import com.ishland.c2me.base.common.scheduler.NeighborLockingUtils;
import com.ishland.c2me.base.common.scheduler.SchedulingManager;
import com.ishland.c2me.threading.lighting.mixin.starlight.access.IStarLightInterface;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class StarLightQueue {

    private final Long2ObjectLinkedOpenHashMap<ChunkTaskSet> chunkTasks = new Long2ObjectLinkedOpenHashMap<>();
    private final Long2ObjectLinkedOpenHashMap<CompletableFuture<Void>> scheduledChunks = new Long2ObjectLinkedOpenHashMap<>();
    private final StarLightInterface manager;

    public StarLightQueue(final StarLightInterface manager) {
        this.manager = manager;
    }

    public boolean isEmpty() {
        synchronized (this.chunkTasks) {
            return this.chunkTasks.isEmpty();
        }
    }

    public CompletableFuture<Void> queueBlockChange(final BlockPos pos) {
        final ChunkTaskSet tasks;
        synchronized (this.chunkTasks) {
            tasks = this.chunkTasks.computeIfAbsent(ChunkPos.toLong(pos), ChunkTaskSet::new);
        }
        synchronized (tasks) {
            tasks.changedPositions.add(pos.toImmutable());
        }
        return tasks.onComplete;
    }

    public CompletableFuture<Void> queueSectionChange(final ChunkSectionPos pos, final boolean newEmptyValue) {
        final ChunkTaskSet tasks;
        synchronized (this.chunkTasks) {
            tasks = this.chunkTasks.computeIfAbsent(ChunkPos.toLong(pos.getSectionX(), pos.getSectionZ()), ChunkTaskSet::new);
        }

        synchronized (tasks) {
            if (tasks.changedSectionSet == null) {
                tasks.changedSectionSet = new Boolean[((IStarLightInterface) (Object) this.manager).getMaxSection() - ((IStarLightInterface) (Object) this.manager).getMinSection() + 1];
            }
            tasks.changedSectionSet[pos.getY() - ((IStarLightInterface) (Object) this.manager).getMinSection()] = Boolean.valueOf(newEmptyValue);
        }

        return tasks.onComplete;
    }

    public CompletableFuture<Void> queueChunkLighting(final ChunkPos pos, final Runnable lightTask) {
        final ChunkTaskSet tasks;
        synchronized (this.chunkTasks) {
            tasks = this.chunkTasks.computeIfAbsent(pos.toLong(), ChunkTaskSet::new);
        }
        synchronized (tasks) {
            if (tasks.lightTasks == null) {
                tasks.lightTasks = new ArrayList<>();
            }
            tasks.lightTasks.add(lightTask);
        }

        return tasks.onComplete;
    }

    public CompletableFuture<Void> queueChunkSkylightEdgeCheck(final ChunkSectionPos pos, final ShortCollection sections) {
        final ChunkTaskSet tasks;
        synchronized (this.chunkTasks) {
            tasks = this.chunkTasks.computeIfAbsent(ChunkPos.toLong(pos.getSectionX(), pos.getSectionZ()), ChunkTaskSet::new);
        }

        synchronized (tasks) {
            ShortOpenHashSet queuedEdges = tasks.queuedEdgeChecksSky;
            if (queuedEdges == null) {
                queuedEdges = tasks.queuedEdgeChecksSky = new ShortOpenHashSet();
            }
            queuedEdges.addAll(sections);
        }

        return tasks.onComplete;
    }

    public CompletableFuture<Void> queueChunkBlocklightEdgeCheck(final ChunkSectionPos pos, final ShortCollection sections) {
        final ChunkTaskSet tasks;
        synchronized (this.chunkTasks) {
            tasks = this.chunkTasks.computeIfAbsent(ChunkPos.toLong(pos.getSectionX(), pos.getSectionZ()), ChunkTaskSet::new);
        }

        synchronized (tasks) {
            ShortOpenHashSet queuedEdges = tasks.queuedEdgeChecksBlock;
            if (queuedEdges == null) {
                queuedEdges = tasks.queuedEdgeChecksBlock = new ShortOpenHashSet();
            }
            queuedEdges.addAll(sections);
        }

        return tasks.onComplete;
    }

    public void removeChunk(final ChunkPos pos) {
        final ChunkTaskSet tasks;
        synchronized (this) {
            tasks = this.chunkTasks.remove(CoordinateUtils.getChunkKey(pos));
        }
        if (tasks != null) {
            tasks.onComplete.complete(null);
        }
    }

    public ChunkTaskSet removeFirstTask() {
        if (this.chunkTasks.isEmpty()) {
            return null;
        }
        synchronized (this.chunkTasks) {
            if (this.chunkTasks.isEmpty()) {
                return null;
            }
            return this.chunkTasks.removeFirst();
        }
    }

    public void scheduleAll() {
        if (this.manager.getWorld() instanceof ServerWorld world) {
            final SchedulingManager schedulingManager = ((IVanillaChunkManager) world.getChunkManager().threadedAnvilChunkStorage).c2me$getSchedulingManager();
            List<ChunkTaskSet> tasks = new ArrayList<>(this.chunkTasks.size());
            synchronized (this.chunkTasks) {
                final ObjectBidirectionalIterator<Long2ObjectMap.Entry<ChunkTaskSet>> iterator = this.chunkTasks.long2ObjectEntrySet().fastIterator();
                while (iterator.hasNext()) {
                    final Long2ObjectMap.Entry<ChunkTaskSet> entry = iterator.next();
                    final CompletableFuture<Void> future = this.scheduledChunks.get(entry.getLongKey());
                    if (future == null || future.isDone()) {
                        tasks.add(entry.getValue());
                        iterator.remove();
                    }
                }
            }
            synchronized (this.scheduledChunks) {
                this.scheduledChunks.values().removeIf(CompletableFuture::isDone);
                for (final ChunkTaskSet taskSet : tasks) {
                    if (this.scheduledChunks.get(taskSet.chunkPos) != null) throw new AssertionError();
                    final CompletableFuture<Void> future = NeighborLockingUtils.runChunkGenWithLock(
                            new ChunkPos(taskSet.chunkPos),
                            ChunkStatus.LIGHT,
                            null,
                            2,
                            schedulingManager,
                            false,
                            () -> CompletableFuture.supplyAsync(() -> {
                                SkyStarLightEngine skyStarLightEngine = null;
                                BlockStarLightEngine blockStarLightEngine = null;
                                try {
                                    //noinspection DataFlowIssue
                                    skyStarLightEngine = ((IStarLightInterface) (Object) this.manager).invokeGetSkyLightEngine();
                                    blockStarLightEngine = ((IStarLightInterface) (Object) this.manager).invokeGetBlockLightEngine();
                                    this.handleUpdateInternal(skyStarLightEngine, blockStarLightEngine, taskSet);
                                } finally {
                                    //noinspection ConstantValue
                                    if (skyStarLightEngine != null)
                                        ((IStarLightInterface) (Object) this.manager).invokeReleaseSkyLightEngine(skyStarLightEngine);
                                    //noinspection ConstantValue
                                    if (blockStarLightEngine != null)
                                        ((IStarLightInterface) (Object) this.manager).invokeReleaseBlockLightEngine(blockStarLightEngine);
                                }
                                return taskSet.onComplete;
                            }, GlobalExecutors.executor).thenCompose(Function.identity())
                    );
                    this.scheduledChunks.put(taskSet.chunkPos, future);
                }
            }
        } else {
            ChunkTaskSet taskSet;
            SkyStarLightEngine skyStarLightEngine = null;
            BlockStarLightEngine blockStarLightEngine = null;
            try {
                //noinspection DataFlowIssue
                skyStarLightEngine = ((IStarLightInterface) (Object) this.manager).invokeGetSkyLightEngine();
                blockStarLightEngine = ((IStarLightInterface) (Object) this.manager).invokeGetBlockLightEngine();
                while ((taskSet = this.removeFirstTask()) != null) {
                    this.handleUpdateInternal(skyStarLightEngine, blockStarLightEngine, taskSet);
                }
            } finally {
                //noinspection ConstantValue
                if (skyStarLightEngine != null)
                    ((IStarLightInterface) (Object) this.manager).invokeReleaseSkyLightEngine(skyStarLightEngine);
                //noinspection ConstantValue
                if (blockStarLightEngine != null)
                    ((IStarLightInterface) (Object) this.manager).invokeReleaseBlockLightEngine(blockStarLightEngine);
            }
        }
    }

    private void handleUpdateInternal(SkyStarLightEngine skyEngine, BlockStarLightEngine blockEngine, StarLightQueue.ChunkTaskSet task) {
        if (task.lightTasks != null) {
            for (Runnable run : task.lightTasks) {
                run.run();
            }
        }

        int chunkX = CoordinateUtils.getChunkX(task.chunkPos);
        int chunkZ = CoordinateUtils.getChunkZ(task.chunkPos);
        Set<BlockPos> positions = task.changedPositions;
        Boolean[] sectionChanges = task.changedSectionSet;
        if (skyEngine != null && (!positions.isEmpty() || sectionChanges != null)) {
            skyEngine.blocksChangedInChunk(this.manager.getLightAccess(), chunkX, chunkZ, positions, sectionChanges);
        }

        if (blockEngine != null && (!positions.isEmpty() || sectionChanges != null)) {
            blockEngine.blocksChangedInChunk(this.manager.getLightAccess(), chunkX, chunkZ, positions, sectionChanges);
        }

        if (skyEngine != null && task.queuedEdgeChecksSky != null) {
            skyEngine.checkChunkEdges(this.manager.getLightAccess(), chunkX, chunkZ, task.queuedEdgeChecksSky);
        }

        if (blockEngine != null && task.queuedEdgeChecksBlock != null) {
            blockEngine.checkChunkEdges(this.manager.getLightAccess(), chunkX, chunkZ, task.queuedEdgeChecksBlock);
        }

        task.onComplete.complete(null);
    }

    public ChunkTaskSet takeChunkTasks(long pos) {
        synchronized (this.chunkTasks) {
            return this.chunkTasks.remove(pos);
        }
    }

    public static final class ChunkTaskSet {

        public final Set<BlockPos> changedPositions = new ObjectOpenHashSet<>();
        public Boolean[] changedSectionSet;
        public ShortOpenHashSet queuedEdgeChecksSky;
        public ShortOpenHashSet queuedEdgeChecksBlock;
        public List<Runnable> lightTasks;

        public final CompletableFuture<Void> onComplete = new CompletableFuture<>();

        public final long chunkPos;

        public ChunkTaskSet(final long chunkPos) {
            this.chunkPos = chunkPos;
        }
    }
}
