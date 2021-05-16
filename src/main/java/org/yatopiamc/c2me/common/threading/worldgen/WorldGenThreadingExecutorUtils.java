package org.yatopiamc.c2me.common.threading.worldgen;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.threadly.concurrent.TaskPriority;
import org.yatopiamc.c2me.common.config.C2MEConfig;

public class WorldGenThreadingExecutorUtils {

    public static final C2MEWorldGenPriorityExecutor mainExecutor = new C2MEWorldGenPriorityExecutor(
            C2MEConfig.threadedWorldGenConfig.parallelism,
            TaskPriority.High,
            10,
            new ThreadFactoryBuilder().setDaemon(true).setPriority(Thread.NORM_PRIORITY - 1).setNameFormat("C2ME world gen worker #%d").build()
    );


}
