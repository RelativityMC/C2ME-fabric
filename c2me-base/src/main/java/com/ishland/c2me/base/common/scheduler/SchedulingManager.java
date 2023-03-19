package com.ishland.c2me.base.common.scheduler;

import com.ishland.c2me.base.common.structs.DynamicPriorityQueue;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulingManager {

    public static final int MAX_LEVEL = ThreadedAnvilChunkStorage.MAX_LEVEL + 1;
    private final DynamicPriorityQueue<ScheduledTask> queue = new DynamicPriorityQueue<>(MAX_LEVEL + 1);
    private final Long2ReferenceOpenHashMap<ObjectArraySet<ScheduledTask>> pos2Tasks = new Long2ReferenceOpenHashMap<>();
    private final Long2IntOpenHashMap priorities = new Long2IntOpenHashMap();
    private final AtomicInteger scheduledCount = new AtomicInteger(0);
    private final AtomicBoolean scheduled = new AtomicBoolean(false);

    private final Executor executor;
    private final int maxScheduled;

    {
        priorities.defaultReturnValue(MAX_LEVEL);
    }

    public SchedulingManager(Executor executor, int maxScheduled) {
        this.executor = executor;
        this.maxScheduled = maxScheduled;
    }

    public void enqueue(SchedulingAsyncCombinedLock<?> lock) {
        this.executor.execute(() -> {
            queue.enqueue(lock, priorities.get(lock.centerPos()));
            pos2Tasks.computeIfAbsent(lock.centerPos(), unused -> new ObjectArraySet<>()).add(lock);
            scheduleExecution();
        });
    }

    public void updatePriority(long pos, int priority) {
        this.executor.execute(() -> {
            if (priorities.get(pos) == priority) return;
            if (priority < MAX_LEVEL) {
                priorities.put(pos, priority);
            } else {
                priorities.remove(pos);
            }
            final ObjectArraySet<ScheduledTask> locks = this.pos2Tasks.get(pos);
            if (locks != null) {
                for (ScheduledTask lock : locks) {
                    queue.changePriority(lock, priority);
                }
            }
        });
    }

    private void scheduleExecution() {
        if (scheduledCount.get() < maxScheduled && scheduled.compareAndSet(false, true)) {
            this.executor.execute(() -> {
                while (scheduledCount.get() < maxScheduled && scheduleExecutionInternal()) {
                    scheduledCount.incrementAndGet();
                }
                scheduled.set(false);
            });
        }
    }

    private boolean scheduleExecutionInternal() {
        final ScheduledTask lock = queue.dequeue();
        if (lock != null) {
            this.pos2Tasks.get(lock.centerPos()).remove(lock);
            runPos2TasksMaintenance(lock.centerPos());
            if (lock.trySchedule()) {
                lock.addPostAction(() -> {
                    scheduledCount.decrementAndGet();
                    scheduleExecution();
                });
                return true;
            }
        }
        return false;
    }

    private void runPos2TasksMaintenance(long pos) {
        final ObjectArraySet<ScheduledTask> locks = this.pos2Tasks.get(pos);
        if (locks != null && locks.isEmpty()) {
            this.pos2Tasks.remove(pos);
        }
    }

}
