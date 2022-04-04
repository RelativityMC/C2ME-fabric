package com.ishland.c2me.natives.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.util.internal.PlatformDependent;

import java.lang.ref.Cleaner;

public class Cleaners {

    private static final Cleaner CLEANER = Cleaner.create(new ThreadFactoryBuilder().setNameFormat("C2ME Natives Cleaner").setDaemon(true).build());

    public static void register(Object o, long ptr) {
        CLEANER.register(o, () -> PlatformDependent.freeMemory(ptr));
    }

}
