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
import com.ishland.c2me.opts.dfc.common.gen.jvm.util.DfcObjectCache;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class NoisePosVanillaInterface implements DensityFunction.NoisePos {

    private int x;
    private int y;
    private int z;
    private EvalType type;
    private DfcObjectCache cache = DfcObjectCache.Noop.INSTANCE;

    public NoisePosVanillaInterface() {
    }

    public void ensureUninitialized() {
        if (type != null) throw new IllegalStateException("double use");
    }

    private void ensureInitialized() {
        if (type == null) throw new IllegalStateException("uninitialized use");
    }

    public NoisePosVanillaInterface at(int x, int y, int z, EvalType type, DfcObjectCache cache) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = Objects.requireNonNull(type);
        this.cache = Objects.requireNonNull(cache);
        return this;
    }

    @Override
    public int blockX() {
        ensureInitialized();
        return x;
    }

    @Override
    public int blockY() {
        ensureInitialized();
        return y;
    }

    @Override
    public int blockZ() {
        ensureInitialized();
        return z;
    }

    public EvalType getType() {
        ensureInitialized();
        return type;
    }

    public void deInit() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.type = null;
        this.cache = DfcObjectCache.Noop.INSTANCE;
    }

}
