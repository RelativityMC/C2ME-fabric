package com.ishland.c2me.common.perftracking;

import java.util.function.Supplier;

public interface PerfTracked {

    static <T> Supplier<T> wrap(Supplier<T> supplier, Runnable postAction) {
        class WrappedSupplier implements Supplier<T> {

            @Override
            public T get() {
                try {
                    return supplier.get();
                } finally {
                    postAction.run();
                }
            }
        }
        return new WrappedSupplier();
    }

    static Runnable wrap(Runnable runnable, Runnable postAction) {
        class WrappedRunnable implements Runnable, PerfTracked {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                    postAction.run();
                }
            }
        }
        return new WrappedRunnable();
    }

}
