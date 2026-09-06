package com.ishland.c2me.opts.accel.vulkan.common.gen;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SubmissionPermits {

    private final AtomicInteger available;
    private final Runnable onRelease;

    public SubmissionPermits(int permits, Runnable onRelease) {
        this.available = new AtomicInteger(permits);
        this.onRelease = onRelease;
    }

    public int available() {
        return this.available.get();
    }

    public Borrowed tryBorrow() {
        if (this.available.decrementAndGet() < 0) {
            this.available.incrementAndGet();
            return null;
        }
        return new Borrowed();
    }

    public class Borrowed implements Closeable {

        private final AtomicBoolean released = new AtomicBoolean(false);

        @Override
        public void close() {
            if (!this.released.compareAndSet(false, true)) return;
            SubmissionPermits.this.available.incrementAndGet();
            SubmissionPermits.this.onRelease.run();
        }
    }

}
