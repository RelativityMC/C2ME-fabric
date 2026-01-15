package com.ishland.c2me.base.common.metrics;

import java.util.Arrays;

/**
 * A simple ring buffer for storing historical metric values
 */
public class RingBuffer {

    private final double[] buffer;
    private int index = 0;
    private int count = 0;
    private boolean full = false;

    public RingBuffer(int capacity) {
        this.buffer = new double[capacity];
    }

    public void add(double value) {
        buffer[index] = value;
        index = (index + 1) % buffer.length;
        if (count < buffer.length) {
            count++;
        } else {
            full = true;
        }
    }

    public double getLast() {
        if (count == 0) return 0.0;
        return buffer[(index - 1 + buffer.length) % buffer.length];
    }

    public double[] getValues() {
        double[] result = new double[count];
        if (full) {
            System.arraycopy(buffer, index, result, 0, buffer.length - index);
            System.arraycopy(buffer, 0, result, buffer.length - index, index);
        } else {
            System.arraycopy(buffer, 0, result, 0, count);
        }
        return result;
    }

    public int size() {
        return count;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public void clear() {
        Arrays.fill(buffer, 0.0);
        index = 0;
        count = 0;
        full = false;
    }

    public double getAverage() {
        if (count == 0) return 0.0;
        double sum = 0.0;
        double[] values = getValues();
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    public double getMin() {
        if (count == 0) return 0.0;
        double min = Double.MAX_VALUE;
        double[] values = getValues();
        for (double value : values) {
            if (value < min) min = value;
        }
        return min;
    }

    public double getMax() {
        if (count == 0) return 0.0;
        double max = Double.MIN_VALUE;
        double[] values = getValues();
        for (double value : values) {
            if (value > max) max = value;
        }
        return max;
    }

}