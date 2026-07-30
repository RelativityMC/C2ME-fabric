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
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public interface OpenCLCGenContext {
    String signature = "(const sample_int32_ctx_t ctx)";
    String filler = "nop();".repeat(200) + "\n";

    String nextMethodName();

    String nextMethodName(String suffix);

    ValuesMethodDefD newMethod(AstNode node);

    String getFillerOrNot();

    String callDelegate(ValuesMethodDefD target);

    int allocGlobalDynamicData(Object data);

    int allocGlobalConstData(byte[] data, int alignment);

    int allocGlobalConstDataObject(Object obj);

    int getGlobalDynamicDataOffset(Object data);

    String getCachedSplineMethod(Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper> spline, boolean cache1);

    void cacheSplineMethod(Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper> spline, String method, boolean cache1);

    int registerFlatCache(CacheLikeNode node);

    int registerCache2d(CacheLikeNode node);

    int registerInterpolator(CacheLikeNode node);

    void appendRaw(String raw);
}
