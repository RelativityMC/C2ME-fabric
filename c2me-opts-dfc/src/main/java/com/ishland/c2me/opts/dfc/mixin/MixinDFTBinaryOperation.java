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

package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.opts.dfc.common.ducks.IDfcObjectCacheCapable;
import com.ishland.c2me.opts.dfc.common.gen.jvm.util.DfcObjectCache;
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

            DfcObjectCache dfcObjectCache = applier instanceof IDfcObjectCacheCapable arrayCacheCapable ? arrayCacheCapable.c2me$getDfcObjectCache() : null;

            if (dfcObjectCache != null) {
                ds = dfcObjectCache.getDoubleArray(densities.length, false);
            } else {
                ds = new double[densities.length];
            }

            this.argument2.fill(ds, applier);

            for (int i = 0; i < densities.length; i++) {
                densities[i] += ds[i];
            }

            if (dfcObjectCache != null) {
                dfcObjectCache.recycle(ds);
            }

            ci.cancel();
        }
    }

}
