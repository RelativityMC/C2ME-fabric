package com.ishland.c2me.threading.worldgen.common;

import com.google.common.base.Preconditions;
import com.ishland.c2me.base.common.scheduler.LockTokenImpl;
import com.ishland.c2me.base.common.scheduler.ScheduledTask;
import com.ishland.c2me.base.common.scheduler.SchedulingManager;
import com.ishland.flowsched.executor.LockToken;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.ishland.c2me.threading.worldgen.common.ChunkStatusUtils.ChunkStatusThreadingType.PARALLELIZED;
import static com.ishland.c2me.threading.worldgen.common.ChunkStatusUtils.ChunkStatusThreadingType.SINGLE_THREADED;

public class ChunkStatusUtils {

    public static final BooleanSupplier FALSE_SUPPLIER = () -> false;

    public static ChunkStatusThreadingType getThreadingType(final ChunkStatus status) {
        if (status.equals(ChunkStatus.STRUCTURE_STARTS)
                || status.equals(ChunkStatus.STRUCTURE_REFERENCES)
                || status.equals(ChunkStatus.BIOMES)
                || status.equals(ChunkStatus.NOISE)
                || status.equals(ChunkStatus.SPAWN)
                || status.equals(ChunkStatus.SURFACE)
                || status.equals(ChunkStatus.CARVERS)) {
            return PARALLELIZED;
        } else if (status.equals(ChunkStatus.FEATURES)) {
            return Config.allowThreadedFeatures ? PARALLELIZED : SINGLE_THREADED;
        } else if (status.equals(ChunkStatus.INITIALIZE_LIGHT) ||
                   status.equals(ChunkStatus.LIGHT)) {
            return PARALLELIZED;
        }
        return PARALLELIZED;
    }

    public static <T> CompletableFuture<T> runChunkGenWithLock(ChunkPos target, ChunkStatus status, int radius, SchedulingManager schedulingManager, ChunkStatusThreadingType threadingType, Supplier<CompletableFuture<T>> action) {
        Preconditions.checkNotNull(status);
//        if (radius == 0)
//            return StageSupport.tryWith(chunkLock.acquireLock(target), unused -> action.get()).toCompletableFuture().thenCompose(Function.identity());

        ObjectArrayList<LockToken> lockTargets = new ObjectArrayList<>((2 * radius + 1) * (2 * radius + 1) + 1);
        for (int x = target.x - radius; x <= target.x + radius; x++)
            for (int z = target.z - radius; z <= target.z + radius; z++)
                lockTargets.add(new LockTokenImpl(schedulingManager.getId(), ChunkPos.toLong(x, z), LockTokenImpl.Usage.WORLDGEN));

        if (threadingType == SINGLE_THREADED) {
            lockTargets.add(new LockTokenImpl(schedulingManager.getId(), ChunkPos.MARKER, LockTokenImpl.Usage.WORLDGEN));
        }

        final ScheduledTask<T> task = new ScheduledTask<>(
                target.toLong(),
                action,
                lockTargets.toArray(LockToken[]::new));
        schedulingManager.enqueue(task);
        return task.getFuture();
    }

    public static boolean isCancelled(ChunkHolder holder, ChunkStatus targetStatus) {
        return ChunkLevels.getStatus(holder.getLevel()).getIndex() < targetStatus.getIndex();
    }

    public enum ChunkStatusThreadingType {

        PARALLELIZED(),
        SINGLE_THREADED,
        AS_IS;

    }
}
