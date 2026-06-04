/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.notickvd.common.smooth_sending_rate;

// Taken from the Linux kernel loadavg
public class ExponentialMovingAverage {
    private final double expRise;
    private final double expFall;
    private double oldValue = 0;

    public ExponentialMovingAverage(double tickSeconds, double tauRiseSeconds, double tauFallSeconds) {
        this.expRise = 1.0 / Math.exp(tickSeconds / tauRiseSeconds);
        this.expFall = 1.0 / Math.exp(tickSeconds / tauFallSeconds);
    }

    public double onTick(double value) {
        double newValue =
                value > this.oldValue
                ? this.oldValue * this.expRise + value * (1.0 - this.expRise)
                : this.oldValue * this.expFall + value * (1.0 - this.expFall);
        this.oldValue = newValue;
        return newValue;
    }

    public double getCurrent() {
        return this.oldValue;
    }

}
