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

package com.ishland.c2me.opts.dfc.common.gen.jvm;

import com.google.common.base.Suppliers;
import com.ishland.c2me.base.mixin.access.IChunkNoiseSampler;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ducks.IBlendingAwareVisitor;
import com.ishland.c2me.opts.dfc.common.ducks.ICoordinatesFilling;
import com.ishland.c2me.opts.dfc.common.ducks.IDfcObjectCacheCapable;
import com.ishland.c2me.opts.dfc.common.ducks.IPreloadedCoordinates;
import com.ishland.c2me.opts.dfc.common.gen.jvm.internalapi.IMultiMethod;
import com.ishland.c2me.opts.dfc.common.gen.jvm.internalapi.ISingleMethod;
import com.ishland.c2me.opts.dfc.common.gen.jvm.util.DfcObjectCache;
import com.ishland.c2me.opts.dfc.common.gen.jvm.vif.EachApplierVanillaInterface;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public abstract class AbstractCompiledDensityFunction implements DensityFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCompiledDensityFunction.class);

    protected final Supplier<DensityFunction> blendingFallback;

    public AbstractCompiledDensityFunction(DensityFunction blendingFallback) {
        this(unwrapFallback(blendingFallback));
    }

    public AbstractCompiledDensityFunction(Supplier<DensityFunction> blendingFallback) {
        this.blendingFallback = blendingFallback;
    }

    protected static Supplier<DensityFunction> unwrapFallback(DensityFunction densityFunction) {
        if (densityFunction instanceof AbstractCompiledDensityFunction scdf) {
            return scdf.blendingFallback;
        } else {
            return densityFunction != null ? Suppliers.ofInstance(densityFunction) : null;
        }
    }

    public abstract ISingleMethod getSingleMethod();

    public abstract IMultiMethod getMultiMethod();

    @Override
    public double sample(NoisePos pos) {
        ISingleMethod singleMethod1 = this.getSingleMethod();
        if (singleMethod1 == null || pos.getBlender() != Blender.getNoBlending()) {
            DensityFunction fallback = this.getFallback();
            if (fallback == null) {
                throw new IllegalStateException("blendingFallback is no more");
            }
            return fallback.sample(pos);
        }
        DfcObjectCache cache = pos instanceof IDfcObjectCacheCapable cacheCapable ? cacheCapable.c2me$getDfcObjectCache() : DfcObjectCache.Noop.INSTANCE;
        return singleMethod1.evalSingle(pos.blockX(), pos.blockY(), pos.blockZ(), EvalType.from(pos), cache);
    }

    @Override
    public void fill(double[] densities, EachApplier applier) {
        IMultiMethod multiMethod1 = this.getMultiMethod();
        if (multiMethod1 == null || (applier instanceof ChunkNoiseSampler sampler && sampler.getBlender() != Blender.getNoBlending())) {
            DensityFunction fallback = this.getFallback();
            if (fallback == null) {
                throw new IllegalStateException("blendingFallback is no more");
            }
            fallback.fill(densities, applier);
            return;
        }
        if (applier instanceof EachApplierVanillaInterface vanillaInterface) {
            multiMethod1.evalMulti(densities, vanillaInterface.getX(), vanillaInterface.getY(), vanillaInterface.getZ(), EvalType.from(applier), vanillaInterface.c2me$getDfcObjectCache());
            return;
        }

        DfcObjectCache cache = applier instanceof IDfcObjectCacheCapable cacheCapable ? cacheCapable.c2me$getDfcObjectCache() : DfcObjectCache.Noop.INSTANCE;
        int[] x;
        int[] y;
        int[] z;
        boolean allocatedOnDemand;
        if (applier instanceof IPreloadedCoordinates preloadedCoordinates) {
            x = preloadedCoordinates.c2me$getXArray();
            y = preloadedCoordinates.c2me$getYArray();
            z = preloadedCoordinates.c2me$getZArray();
            allocatedOnDemand = false;
        } else {
            x = cache.getIntArray(densities.length, false);
            y = cache.getIntArray(densities.length, false);
            z = cache.getIntArray(densities.length, false);
            if (applier instanceof ICoordinatesFilling coordinatesFilling) {
                coordinatesFilling.c2me$fillCoordinates(x, y, z);
            } else {
                for (int i = 0; i < densities.length; i ++) {
                    NoisePos pos = applier.at(i);
                    x[i] = pos.blockX();
                    y[i] = pos.blockY();
                    z[i] = pos.blockZ();
                }
            }
            allocatedOnDemand = true;
        }
        try {
            multiMethod1.evalMulti(densities, x, y, z, EvalType.from(applier), cache);
        } finally {
            if (allocatedOnDemand) {
                cache.recycle(x);
                cache.recycle(y);
                cache.recycle(z);
            }
        }
    }

    @Override
    public abstract DensityFunction apply(DensityFunctionVisitor visitor);

    @Override
    public double minValue() {
//        DensityFunction fallback = this.getFallback();
//        return fallback != null ? fallback.minValue() : Double.MIN_VALUE;
        return Double.MIN_VALUE;
    }

    @Override
    public double maxValue() {
//        DensityFunction fallback = this.getFallback();
//        return fallback != null ? fallback.maxValue() : Double.MAX_VALUE;
        return Double.MAX_VALUE;
    }

    @Override
    public CodecHolder<? extends DensityFunction> getCodecHolder() {
        throw new UnsupportedOperationException();
    }

    protected DensityFunction getFallback() {
        return this.blendingFallback != null ? this.blendingFallback.get() : null;
    }
}
