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

package com.ishland.c2me.opts.dfc.common.ducks;

import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import net.minecraft.world.gen.densityfunction.DensityFunction;

public interface IFastCacheLike extends DensityFunction {

    public static final long CACHE_MISS_NAN_BITS = 0x7ffddb972d486a4fL;

    double c2me$getCached(int x, int y, int z, EvalType evalType);

    boolean c2me$getCached(double[] res, int[] x, int[] y, int[] z, EvalType evalType);

    void c2me$cache(int x, int y, int z, EvalType evalType, double cached);

    void c2me$cache(double[] res, int[] x, int[] y, int[] z, EvalType evalType);

    boolean c2me$isActualCache();

    String c2me$describeCacheLike();

    DensityFunction c2me$getDelegate();

    // called by generated code
    DensityFunction c2me$withDelegate(DensityFunction delegate);

}
