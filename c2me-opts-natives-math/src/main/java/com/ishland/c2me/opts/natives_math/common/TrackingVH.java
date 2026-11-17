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

package com.ishland.c2me.opts.natives_math.common;

import com.ishland.c2me.opts.natives_math.common.ducks.INativePointer;
import com.ishland.c2me.base.common.integration.lithostitched.FNLBindings;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class TrackingVH {

    public static final int THRESHOLD = 65536;

//    public static final VarHandle VH_DoublePerlinNoiseSampler;
    public static final VarHandle VH_FastNoiseConfig;

    static {
        try {
//            VH_DoublePerlinNoiseSampler = MethodHandles.lookup()
//                    .findVarHandle(DoublePerlinNoiseSampler.class, "c2me$sampledCount", int.class);
            if (FNLBindings.CLASS_FastNoiseConfig != null && INativePointer.class.isAssignableFrom(FNLBindings.CLASS_FastNoiseConfig)) {
                VH_FastNoiseConfig = MethodHandles.lookup()
                        .findVarHandle(FNLBindings.CLASS_FastNoiseConfig, "c2me$sampledCount", int.class);
            } else  {
                VH_FastNoiseConfig = null;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
