package org.yatopiamc.c2me.common.threading.worldgen;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.TaskPriority;

import java.util.concurrent.ThreadFactory;

public class C2MEWorldGenPriorityExecutor extends PriorityScheduler {

    public C2MEWorldGenPriorityExecutor(int poolSize, TaskPriority defaultPriority, long maxWaitForLowPriorityInMs, ThreadFactory threadFactory) {
        super(poolSize, defaultPriority, maxWaitForLowPriorityInMs, threadFactory);
    }

}
