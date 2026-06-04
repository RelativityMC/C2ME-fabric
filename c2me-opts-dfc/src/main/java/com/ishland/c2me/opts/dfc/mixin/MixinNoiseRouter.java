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

import com.ishland.c2me.opts.dfc.common.ducks.NoiseRouterExtension;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseRouter.class)
public class MixinNoiseRouter implements NoiseRouterExtension {

    @Unique
    private DensityFunction c2me$finalFinalDensity;

    @Unique
    private NoiseRouter c2me$originalNoiseRouter;

    @Override
    public DensityFunction c2me$getFinalFinalDensity() {
        return this.c2me$finalFinalDensity;
    }

    @Override
    public void c2me$setFinalFinalDensity(DensityFunction densityFunction) {
        this.c2me$finalFinalDensity = densityFunction;
    }

    @Unique
    public void c2me$setOriginalNoiseRouter(NoiseRouter originalNoiseRouter) {
        this.c2me$originalNoiseRouter = originalNoiseRouter;
    }

    @Unique
    public NoiseRouter c2me$getOriginalNoiseRouter() {
        return this.c2me$originalNoiseRouter;
    }

    @Inject(method = "apply", at = @At("RETURN"))
    private void postApply(DensityFunction.DensityFunctionVisitor visitor, CallbackInfoReturnable<NoiseRouter> cir) {
        if (this.c2me$finalFinalDensity != null) {
            ((MixinNoiseRouter) (Object) cir.getReturnValue()).c2me$finalFinalDensity = this.c2me$finalFinalDensity.apply(visitor);
        }
        ((MixinNoiseRouter) (Object) cir.getReturnValue()).c2me$originalNoiseRouter = this.c2me$originalNoiseRouter;
    }

}
