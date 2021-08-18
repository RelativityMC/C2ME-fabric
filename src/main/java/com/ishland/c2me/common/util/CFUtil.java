package com.ishland.c2me.common.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;

public class CFUtil {

    public static <T> T join(CompletableFuture<T> future) {
        while (!future.isDone()) {
            LockSupport.parkNanos("Waiting for future", 100000L);
        }
        return future.join();
    }

}
