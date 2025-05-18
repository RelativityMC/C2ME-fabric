package com.ishland.c2me.rewrites.chunksystem.common.structs;

import com.ishland.c2me.base.common.GlobalExecutors;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

public class ChunkSystemExecutors {

    public static final ThreadLocal<Queue<Runnable>> CONSOLIDATING_QUEUE = new ThreadLocal<>();

    public static final Executor backingBackgroundExecutor = GlobalExecutors.prioritizedScheduler.executor(15);
    public static final Scheduler backgroundScheduler = Schedulers.from(backingBackgroundExecutor);
    public static final Executor consolidatingBackgroundExecutor = command -> {
        Queue<Runnable> runnables = CONSOLIDATING_QUEUE.get();
        if (runnables == null) { // first entry
            consolidatingRoot(command);
            CONSOLIDATING_QUEUE.remove(); // get() initielizes threadlocal
            return;
        }
        runnables.add(command);
    };
    public static final Scheduler consolidatingBackgroundScheduler = Schedulers.from(consolidatingBackgroundExecutor);

    private static void consolidatingRoot(Runnable initialCommand) {
        backingBackgroundExecutor.execute(() -> {
            Queue<Runnable> runnables = CONSOLIDATING_QUEUE.get();
            if (runnables != null) {
                new Throwable("CONSOLIDATING_QUEUE leak").printStackTrace();
                try {
                    initialCommand.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                return;
            }

            CONSOLIDATING_QUEUE.set(runnables = new ArrayDeque<>());
            runnables.add(initialCommand);
            try {
                while (!runnables.isEmpty()) {
                    try {
                        runnables.remove().run();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            } finally {
                if (!runnables.isEmpty()) {
                    new Throwable("runnable leak").printStackTrace();
                }
                CONSOLIDATING_QUEUE.remove();
            }
        });
    }

}
