package com.ishland.c2me.common.perftracking;

import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

public class PerfTrackingRegistry {

    public static final Set<PerfTrackingObject.PerfTrackingIoWorker> ioWorkers = Sets.newConcurrentHashSet();
    public static final Set<PerfTrackingObject.PerfTrackingTACS> TACs = Sets.newConcurrentHashSet();
    public static final Set<PerfTrackingObject.PerfTrackingThreadExecutor> threadExecutors = Sets.newConcurrentHashSet();

    public static PerfTrackingObject overall(Collection<? extends PerfTrackingObject> collection) {
        return new PerfTrackingObject() {
            @Override
            public double getAverage5s() {
                return collection.stream().mapToDouble(PerfTrackingObject::getAverage5s).sum();
            }

            @Override
            public double getAverage10s() {
                return collection.stream().mapToDouble(PerfTrackingObject::getAverage10s).sum();
            }

            @Override
            public double getAverage1m() {
                return collection.stream().mapToDouble(PerfTrackingObject::getAverage1m).sum();
            }

            @Override
            public double getAverage5m() {
                return collection.stream().mapToDouble(PerfTrackingObject::getAverage5m).sum();
            }

            @Override
            public double getAverage15m() {
                return collection.stream().mapToDouble(PerfTrackingObject::getAverage15m).sum();
            }
        };
    }

}
