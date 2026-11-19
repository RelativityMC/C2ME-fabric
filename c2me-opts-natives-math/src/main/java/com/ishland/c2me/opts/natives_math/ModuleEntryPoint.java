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

package com.ishland.c2me.opts.natives_math;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class ModuleEntryPoint {

    public static final boolean allowAVX512 = new ConfigSystem.ConfigAccessor()
            .key("vanillaWorldGenOptimizations.nativeAcceleration.allowAVX512")
            .comment("""
                    Enable the use of AVX512 for native acceleration
                    
                    Currently, AVX512 implementation is generally slower than AVX2 implementations.
                    If you ever decide to enable this, make sure you have verified that
                    AVX512 implementations are faster on your machine.
                    """)
            .getBoolean(false, false);
    private static final boolean enabled;

    static {
        System.setProperty("com.ishland.c2me.opts.natives_math.duringGameInit", "true");
        boolean configured = new ConfigSystem.ConfigAccessor()
                .key("vanillaWorldGenOptimizations.nativeAcceleration.enabled")
                .comment("""
                        Enable the use of bundled native libraries to accelerate world generation
                        """)
                .getBoolean(true, false);
        boolean actuallyEnabled = false;
        if (configured) {
            try {
                actuallyEnabled = Class.forName("com.ishland.c2me.opts.natives_math.common.NativeLoader").getField("lookup").get(null) != null;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        enabled = actuallyEnabled;
    }

}
