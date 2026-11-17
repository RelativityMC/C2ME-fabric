/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
