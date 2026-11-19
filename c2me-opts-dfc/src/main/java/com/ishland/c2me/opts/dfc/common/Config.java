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

package com.ishland.c2me.opts.dfc.common;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class Config {

    public static final boolean enableBuiltinIntegrations = new ConfigSystem.ConfigAccessor()
            .key("vanillaWorldGenOptimizations.enableBuiltinDFCIntegrations")
            .comment("""
                    Enables the built-in integrations with other worldgen mods in density function compiler.
                    
                    Depends on vanillaWorldGenOptimizations.useDensityFunctionCompiler
                    """)
            .getBoolean(true, false);

    public static void init() {
        // intentionally empty
    }

    private Config() {
    }

}
