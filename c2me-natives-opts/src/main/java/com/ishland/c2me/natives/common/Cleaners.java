package com.ishland.c2me.natives.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.lang.ref.Cleaner;

public class Cleaners {

    private static final Cleaner CLEANER = Cleaner.create(new ThreadFactoryBuilder().setNameFormat("C2ME Natives Cleaner").setDaemon(true).build());

    public static void register(Object o, long... ptrs) {
        CLEANER.register(o, () -> {
            for (long ptr : ptrs) {
                UnsafeUtil.getInstance().freeMemory(ptr);
            }
        });
    }

}
