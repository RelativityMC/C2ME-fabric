/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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

    public static final boolean useEndBiomeCache = new ConfigSystem.ConfigAccessor()
            .key("vanillaWorldGenOptimizations.useEndBiomeCache")
            .comment("""
                    Whether to enable End Biome Cache to accelerate The End worldgen\s
                    This is no longer included in lithium-fabric\s
                    (may cause incompatibility with other mods)
                    """)
            .incompatibleMod("biolith", "*")
            .getBoolean(true, false);

    public static final boolean optimizeStructureWeightSampler = new ConfigSystem.ConfigAccessor()
            .key("vanillaWorldGenOptimizations.optimizeStructureWeightSampler")
            .comment("""
                    Whether to enable StructureWeightSampler optimizations to accelerate world generation
                    """)
            .incompatibleMod("porting_lib", "*")
            .getBoolean(true, false);

}
