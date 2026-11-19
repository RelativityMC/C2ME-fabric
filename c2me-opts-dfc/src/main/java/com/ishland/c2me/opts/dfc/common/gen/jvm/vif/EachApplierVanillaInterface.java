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

package com.ishland.c2me.opts.dfc.common.gen.jvm.vif;

import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ducks.IDfcObjectCacheCapable;
import com.ishland.c2me.opts.dfc.common.gen.jvm.util.DfcObjectCache;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Objects;

public class EachApplierVanillaInterface implements DensityFunction.EachApplier, IDfcObjectCacheCapable {

    private final int[] x;
    private final int[] y;
    private final int[] z;
    private final EvalType type;
    private final DfcObjectCache cache;

    private final NoisePosVanillaInterface noisePosVanillaInterface = new NoisePosVanillaInterface();

    public EachApplierVanillaInterface(int[] x, int[] y, int[] z, EvalType type) {
        this(x, y, z, type, DfcObjectCache.Noop.INSTANCE);
    }

    public EachApplierVanillaInterface(int[] x, int[] y, int[] z, EvalType type, DfcObjectCache cache) {
        this.x = Objects.requireNonNull(x);
        this.y = Objects.requireNonNull(y);
        this.z = Objects.requireNonNull(z);
        this.type = Objects.requireNonNull(type);
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public DensityFunction.NoisePos at(int index) {
        return this.noisePosVanillaInterface.at(x[index], y[index], z[index], type, cache);
    }

    @Override
    public void fill(double[] densities, DensityFunction densityFunction) {
        for (int i = 0; i < x.length; i++) {
            densities[i] = densityFunction.sample(this.at(i));
        }
        this.noisePosVanillaInterface.deInit();
    }

    public int[] getX() {
        return x;
    }

    public int[] getY() {
        return y;
    }

    public int[] getZ() {
        return z;
    }

    public EvalType getType() {
        return type;
    }

    @Override
    public DfcObjectCache c2me$getDfcObjectCache() {
        return this.cache;
    }
}
