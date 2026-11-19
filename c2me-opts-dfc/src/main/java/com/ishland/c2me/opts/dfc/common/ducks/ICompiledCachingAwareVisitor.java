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

import com.ishland.c2me.opts.dfc.common.gen.jvm.CompiledEntry;
import com.ishland.c2me.opts.dfc.common.gen.jvm.internalapi.ArgumentVisitor;
import net.minecraft.world.gen.densityfunction.DensityFunction;

public interface ICompiledCachingAwareVisitor {

    CompiledEntry c2me$visitIfAbsent(CompiledEntry entry, ArgumentVisitor visitor);

    public static ArgumentVisitor c2me$getArgumentVisitor(DensityFunction.DensityFunctionVisitor visitor) {
        return next -> {
            if (next instanceof DensityFunction df) {
                return df.apply(visitor);
            }
            if (next instanceof DensityFunction.Noise noise) {
                return visitor.apply(noise);
            }
            return next;
        };
    }

}
