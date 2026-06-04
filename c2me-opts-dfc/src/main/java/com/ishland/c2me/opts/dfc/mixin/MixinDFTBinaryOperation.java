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

package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.opts.dfc.common.ducks.IArrayCacheCapable;
import com.ishland.c2me.opts.dfc.common.util.ArrayCache;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DensityFunctionTypes.BinaryOperation.class)
public class MixinDFTBinaryOperation {

    @Shadow @Final private DensityFunctionTypes.BinaryOperationLike.Type type;

    @Shadow @Final private DensityFunction argument1;

    @Shadow @Final private DensityFunction argument2;

    @Inject(method = "fill", at = @At("HEAD"), cancellable = true)
    private void wrapFill(double[] densities, DensityFunction.EachApplier applier, CallbackInfo ci) {
        if (this.type == DensityFunctionTypes.BinaryOperationLike.Type.ADD) {
            this.argument1.fill(densities, applier);
            double[] ds;

            ArrayCache arrayCache = applier instanceof IArrayCacheCapable arrayCacheCapable ? arrayCacheCapable.c2me$getArrayCache() : null;

            if (arrayCache != null) {
                ds = arrayCache.getDoubleArray(densities.length, false);
            } else {
                ds = new double[densities.length];
            }

            this.argument2.fill(ds, applier);

            for (int i = 0; i < densities.length; i++) {
                densities[i] += ds[i];
            }

            if (arrayCache != null) {
                arrayCache.recycle(ds);
            }

            ci.cancel();
        }
    }

}
