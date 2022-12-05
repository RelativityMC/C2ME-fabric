package com.ishland.c2me.natives.common;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DeferredCompilationUtil {

    private static boolean deferCompilation = true;
    private static Queue<Runnable> deferredCompilationQueue = new ConcurrentLinkedQueue<>();

    public static void deferCompilation(Runnable runnable) {
        if (deferCompilation) {
            deferredCompilationQueue.add(runnable);
        } else {
            runnable.run();
        }
    }

    public static void runDeferredCompilation() {
        deferCompilation = false;
        System.out.println("Running %d deferred compilations".formatted(deferredCompilationQueue.size()));
        Runnable runnable;
        while ((runnable = deferredCompilationQueue.poll()) != null) {
            runnable.run();
        }
    }

}
