package com.ishland.c2me.common.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class IntegerRollingAverage {

    private final AtomicIntegerArray history;
    private final AtomicInteger counter = new AtomicInteger(0);

    public IntegerRollingAverage(int historySize) {
        this.history = new AtomicIntegerArray(historySize);
    }

    public void submit(int num) {
        history.set(counter.getAndIncrement() % history.length(), num);
    }

    public double average() {
        final int i = counter.getPlain();
        final int length = history.length();
        long sum = 0;
        if (i < length) {
            for (int n = 0; n < i; n ++)
                sum += history.get(n);
            return sum / (double) i;
        } else {
            for (int n = 0; n < length; n ++)
                sum += history.get(n);
            return sum / (double) length;
        }
    }

}
