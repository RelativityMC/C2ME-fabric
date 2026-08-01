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

package com.ishland.c2me.opts.dfc.common.gen.opencl;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public interface OpenCLCGenFunctionContext {
    OpenCLCGenContext getGlobalContext();

    FunctionVariant getVariant();

    String nextVarName();

    ValuesMethodDefF64 newVar(AstNode node);

    String newVarUnoptimized(AstNode node);

    String getDelegateVar(ValuesMethodDefF64 target);

    String getCachedSplineVar(Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper> spline);

    void cacheSplineVar(Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper> spline, String varName);

    OpenCLCGenFunctionContext fork();

    String getBody();

    void appendRaw(String raw);

    public enum FunctionVariant {
        UNCACHED("_uncached", true, false, false, false),
        FLATCACHE_ONLY("_flatcache_only", true, true, false, false),
        FULLY_CACHED("_fully_cached", true, true, true, false),
        FULLY_CACHED_EXCEPT_CACHE2D("_fully_cached_except_cache2d", false, true, true, false)
        ;

        public final String suffix;
        public final boolean inDispatcher;
        public final boolean enableFlatCache;
        public final boolean enableAllCache;
        private final boolean disableCache2d;

        FunctionVariant(String suffix, boolean inDispatcher, boolean enableFlatCache, boolean enableAllCache, boolean disableCache2d) {
            this.suffix = suffix;
            this.inDispatcher = inDispatcher;
            this.enableFlatCache = enableFlatCache;
            this.enableAllCache = enableAllCache;
            this.disableCache2d = disableCache2d;
        }

        public boolean useCache2D() {
            return this.enableAllCache && !this.disableCache2d;
        }
    }
}
