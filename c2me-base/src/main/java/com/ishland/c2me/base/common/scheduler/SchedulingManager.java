package com.ishland.c2me.base.common.scheduler;

import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.flowsched.structs.SimpleObjectPool;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulingManager {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    public static final int MAX_LEVEL = ChunkLevels.INACCESSIBLE + 1;
    private final Long2ReferenceOpenHashMap<ObjectArraySet<AbstractPosAwarePrioritizedTask>> pos2Tasks = new Long2ReferenceOpenHashMap<>();
    private final SimpleObjectPool<ObjectArraySet<AbstractPosAwarePrioritizedTask>> pos2TasksPool = new SimpleObjectPool<>(unused -> new ObjectArraySet<>(), ObjectArraySet::clear, ObjectArraySet::clear, 2048);
    private final Long2IntOpenHashMap prioritiesFromLevel = new Long2IntOpenHashMap();
    private final Object schedulingMutex = new Object();
    private final int id = COUNTER.getAndIncrement();
    private ChunkPos currentSyncLoad = null;

    private final Executor executor;

    {
        prioritiesFromLevel.defaultReturnValue(MAX_LEVEL);
    }

    public SchedulingManager(Executor executor) {
        this.executor = executor;
    }

    public void enqueue(AbstractPosAwarePrioritizedTask task) {
        synchronized (this.schedulingMutex) {
            final long pos = task.getPos();
            final ObjectArraySet<AbstractPosAwarePrioritizedTask> locks = this.pos2Tasks.computeIfAbsent(pos, unused -> this.pos2TasksPool.alloc());
            locks.add(task);
            updatePriorityInternal(pos);
        }
        task.addPostExec(() -> {
            synchronized (this.schedulingMutex) {
                final ObjectArraySet<AbstractPosAwarePrioritizedTask> tasks = this.pos2Tasks.get(task.getPos());
                if (tasks != null) {
                    tasks.remove(task);
                    if (tasks.isEmpty()) {
                        this.pos2Tasks.remove(task.getPos());
                        this.pos2TasksPool.release(tasks);
                    }
                }
            }
        });
        GlobalExecutors.prioritizedScheduler.schedule(task);
    }

    public void enqueue(long pos, Runnable command) {
        this.enqueue(new WrappingTask(pos, command));
    }

    public Executor positionedExecutor(long pos) {
        return command -> this.enqueue(pos, command);
    }

    public void updatePriorityFromLevel(long pos, int level) {
        this.executor.execute(() -> {
            synchronized (this.schedulingMutex) {
                if (prioritiesFromLevel.get(pos) == level) return;
                if (level < MAX_LEVEL) {
                    prioritiesFromLevel.put(pos, level);
                } else {
                    prioritiesFromLevel.remove(pos);
                }
                updatePriorityInternal(pos);
            }
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
        final ObjectArraySet<AbstractPosAwarePrioritizedTask> locks = this.pos2Tasks.get(pos);
        if (locks != null) {
            for (AbstractPosAwarePrioritizedTask lock : locks) {
                lock.setPriority(priority);
                GlobalExecutors.prioritizedScheduler.notifyPriorityChange(lock);
            }
        }
    }

    public void setCurrentSyncLoad(ChunkPos pos) {
        executor.execute(() -> {
            synchronized (this.schedulingMutex) {
                if (this.currentSyncLoad != null) {
                    final ChunkPos lastSyncLoad = this.currentSyncLoad;
                    this.currentSyncLoad = null;
                    updateSyncLoadInternal(lastSyncLoad);
                }
                if (pos != null) {
                    this.currentSyncLoad = pos;
                    updateSyncLoadInternal(pos);
                }
            }
        });
    }

    public int getId() {
        return this.id;
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

}
