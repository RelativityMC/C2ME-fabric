package com.ishland.c2me.common.perftracking;

import net.minecraft.util.Identifier;

import java.io.File;

public interface PerfTrackingObject {
    double getAverage5s();

    double getAverage10s();

    double getAverage1m();

    double getAverage5m();

    double getAverage15m();

    interface PerfTrackingIoWorker extends PerfTrackingObject {
        File getDirectory();
    }

    interface PerfTrackingTACS extends PerfTrackingObject {
        Identifier getWorldRegistryKey();
    }

    interface PerfTrackingThreadExecutor extends PerfTrackingObject {
        String getExecutorName();
        String getThreadName();
    }

}
