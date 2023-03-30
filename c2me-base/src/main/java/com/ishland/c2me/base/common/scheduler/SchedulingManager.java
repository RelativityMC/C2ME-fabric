package com.ishland.c2me.base.common.scheduler;

import com.ishland.c2me.base.common.structs.DynamicPriorityQueue;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulingManager {

    public static final int MAX_LEVEL = ThreadedAnvilChunkStorage.MAX_LEVEL + 1;
    private final DynamicPriorityQueue<ScheduledTask> queue = new DynamicPriorityQueue<>(MAX_LEVEL + 1);
    private final Long2ReferenceOpenHashMap<ObjectArraySet<ScheduledTask>> pos2Tasks = new Long2ReferenceOpenHashMap<>();
    private final Long2IntOpenHashMap prioritiesFromLevel = new Long2IntOpenHashMap();
    private final AtomicInteger scheduledCount = new AtomicInteger(0);
    private final AtomicBoolean scheduled = new AtomicBoolean(false);
    private ChunkPos currentSyncLoad = null;

    private final Executor executor;
    private final int maxScheduled;

    {
        prioritiesFromLevel.defaultReturnValue(MAX_LEVEL);
    }

    public SchedulingManager(Executor executor, int maxScheduled) {
        this.executor = executor;
        this.maxScheduled = maxScheduled;
    }

    public void enqueue(SchedulingAsyncCombinedLock<?> lock) {
        this.executor.execute(() -> {
            queue.enqueue(lock, prioritiesFromLevel.get(lock.centerPos()));
            pos2Tasks.computeIfAbsent(lock.centerPos(), unused -> new ObjectArraySet<>()).add(lock);
            scheduleExecution();
        });
    }

    public void updatePriorityFromLevel(long pos, int level) {
        this.executor.execute(() -> {
            if (prioritiesFromLevel.get(pos) == level) return;
            if (level < MAX_LEVEL) {
                prioritiesFromLevel.put(pos, level);
            } else {
                prioritiesFromLevel.remove(pos);
            }
            updatePriorityInternal(pos);
        });
    }

    private void updatePriorityInternal(long pos) {
        int fromLevel = prioritiesFromLevel.get(pos);
        int fromSyncLoad;
        if (currentSyncLoad != null) {
            final int chebyshevDistance = chebyshev(new ChunkPos(pos), currentSyncLoad);
            if (chebyshevDistance <= 8) {
                fromSyncLoad = chebyshevDistance;
//                System.out.println("dist for chunk [%d,%d] is %d".formatted(currentSyncLoad.x, currentSyncLoad.z, chebyshevDistance));
            } else {
                fromSyncLoad = MAX_LEVEL;
            }
        } else {
            fromSyncLoad = MAX_LEVEL;
        }
        int priority = Math.min(fromLevel, fromSyncLoad);
        final ObjectArraySet<ScheduledTask> locks = this.pos2Tasks.get(pos);
        if (locks != null) {
            for (ScheduledTask lock : locks) {
                queue.changePriority(lock, priority);
            }
        }
    }

    public void setCurrentSyncLoad(ChunkPos pos) {
        executor.execute(() -> {
            if (this.currentSyncLoad != null) {
                final ChunkPos lastSyncLoad = this.currentSyncLoad;
                this.currentSyncLoad = null;
                updateSyncLoadInternal(lastSyncLoad);
            }
            if (pos != null) {
                this.currentSyncLoad = pos;
                updateSyncLoadInternal(pos);
            }
        });
    }

    private void updateSyncLoadInternal(ChunkPos pos) {
        long startTime = System.nanoTime();
        for (int xOff = -8; xOff <= 8; xOff++) {
            for (int zOff = -8; zOff <= 8; zOff++) {
                updatePriorityInternal(ChunkPos.toLong(pos.x + xOff, pos.z + zOff));
            }
        }
        long endTime = System.nanoTime();
    }

    private void scheduleExecution() {
        if (scheduledCount.get() < maxScheduled && scheduled.compareAndSet(false, true)) {
            this.executor.execute(() -> {
                ScheduleStatus status;
                while (scheduledCount.get() < maxScheduled && (status = scheduleExecutionInternal()).success) {
                    if (!status.async) scheduledCount.incrementAndGet();
                }
                scheduled.set(false);
            });
        }
    }

    private ScheduleStatus scheduleExecutionInternal() {
        final ScheduledTask lock = queue.dequeue();
        if (lock != null) {
            this.pos2Tasks.get(lock.centerPos()).remove(lock);
            runPos2TasksMaintenance(lock.centerPos());
            if (lock.trySchedule()) {
                final boolean async = lock.isAsync();
                lock.addPostAction(() -> {
                    if (!async) scheduledCount.decrementAndGet();
                    scheduleExecution();
                });
                if (async) {
                    return ScheduleStatus.SCHEDULED_ASYNC;
                } else {
                    return ScheduleStatus.SCHEDULED;
                }
            }
        }
        return ScheduleStatus.NOT_SCHEDULED;
    }

    private static int chebyshev(ChunkPos a, ChunkPos b) {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.z - b.z));
    }

    private static int chebyshev(long a, long b) {
        return Math.max(Math.abs(ChunkPos.getPackedX(a) - ChunkPos.getPackedX(b)), Math.abs(ChunkPos.getPackedZ(a) - ChunkPos.getPackedZ(b)));
    }

    private enum ScheduleStatus {
        SCHEDULED(true, false),
        SCHEDULED_ASYNC(true, true),
        NOT_SCHEDULED(false, false);

        public final boolean success;
        public final boolean async;

        ScheduleStatus(boolean success, boolean async) {
            this.success = success;
            this.async = async;
        }
    };

    private void runPos2TasksMaintenance(long pos) {
        final ObjectArraySet<ScheduledTask> locks = this.pos2Tasks.get(pos);
        if (locks != null && locks.isEmpty()) {
            this.pos2Tasks.remove(pos);
        }
    }

}
