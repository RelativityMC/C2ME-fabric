package com.ishland.c2me.base.common.metrics;

/**
 * A simple stopwatch for measuring elapsed time
 */
public class Stopwatch {

    private long startTime = -1;
    private long endTime = -1;

    public void start() {
        startTime = System.nanoTime();
        endTime = -1;
    }

    public void stop() {
        endTime = System.nanoTime();
    }

    public long elapsedNanos() {
        if (startTime == -1) return 0;
        long end = endTime != -1 ? endTime : System.nanoTime();
        return end - startTime;
    }

    public double elapsedMillis() {
        return elapsedNanos() / 1_000_000.0;
    }

    public void reset() {
        startTime = -1;
        endTime = -1;
    }

    public boolean isRunning() {
        return startTime != -1 && endTime == -1;
    }

}