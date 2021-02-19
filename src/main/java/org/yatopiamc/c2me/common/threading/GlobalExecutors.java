package org.yatopiamc.c2me.common.threading;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class GlobalExecutors {

    public static final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(
            1,
            new ThreadFactoryBuilder().setNameFormat("C2ME scheduler").setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).build()
    );

}
