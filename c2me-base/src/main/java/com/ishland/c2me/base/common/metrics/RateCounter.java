package com.ishland.c2me.base.common.metrics;

/**
 * A rate counter that tracks events per second over a sliding window
 */
public class RateCounter {

    private final RingBuffer buffer;
    private long lastTime = System.nanoTime();

    public RateCounter(int windowSize) {
        this.buffer = new RingBuffer(windowSize);
    }

    public void increment() {
        long now = System.nanoTime();
        long deltaTime = now - lastTime;
        if (deltaTime > 0) {
            double rate = 1_000_000_000.0 / deltaTime; // events per second
            buffer.add(rate);
        }
        lastTime = now;
    }

    public void increment(long count) {
        long now = System.nanoTime();
        long deltaTime = now - lastTime;
        if (deltaTime > 0) {
            double rate = (count * 1_000_000_000.0) / deltaTime; // events per second
            buffer.add(rate);
        }
        lastTime = now;
    }

    public double getRate() {
        return buffer.getLast();
    }

    public double getAverageRate() {
        return buffer.getAverage();
    }

    public void clear() {
        buffer.clear();
    }

}