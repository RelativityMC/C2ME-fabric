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

package com.ishland.c2me.opts.worldgen.vanilla.common;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class Config {

    public static final boolean optimizeAquifer = new ConfigSystem.ConfigAccessor()
            .key("vanillaWorldGenOptimizations.optimizeAquifer")
            .comment("Whether to enable aquifer optimizations to accelerate overworld worldgen\n" +
                    "(may cause incompatibility with other mods)")
            .incompatibleMod("cavetweaks", "*")
            .getBoolean(true, false);

    public static final boolean optimizeStructureWeightSampler = new ConfigSystem.ConfigAccessor()
            .key("vanillaWorldGenOptimizations.optimizeStructureWeightSampler")
            .comment("""
                    Whether to enable StructureWeightSampler optimizations to accelerate world generation
                    """)
            .incompatibleMod("porting_lib", "*")
            .getBoolean(true, false);

}
