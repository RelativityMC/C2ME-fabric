package com.ishland.c2me.common.threading;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlobalExecutors {

    public static final ExecutorService scheduler = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("C2ME scheduler").setDaemon(true).setPriority(Thread.NORM_PRIORITY).build());


}
