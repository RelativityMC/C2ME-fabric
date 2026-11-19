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

package com.ishland.c2me.opts.dfc;

import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.opts.dfc.common.Config;

public class ModuleEntryPoint {

    private static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("vanillaWorldGenOptimizations.useDensityFunctionCompiler")
            .comment("""
                    Whether to use density function compiler to accelerate world generation
                    
                    Density function: https://minecraft.wiki/w/Density_function
                    
                    This functionality compiles density functions from world generation
                    datapacks (including vanilla generation) to JVM bytecode to increase
                    performance by allowing JVM JIT to better optimize the code
                    
                    Currently, all functions provided by vanilla are implemented.
                    Chunk upgrades from pre-1.18 versions are not implemented and will
                    fall back to the unoptimized version of density functions.
                    """)
            .getBoolean(true, false);

    static {
        Config.init();
    }

}
