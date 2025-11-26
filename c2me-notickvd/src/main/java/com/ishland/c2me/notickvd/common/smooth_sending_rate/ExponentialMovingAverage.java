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
