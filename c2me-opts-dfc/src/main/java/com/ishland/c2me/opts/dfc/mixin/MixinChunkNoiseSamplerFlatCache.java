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

import com.ishland.c2me.base.mixin.access.IChunkNoiseSampler;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ducks.IFastCacheLike;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkNoiseSampler.FlatCache.class)
public abstract class MixinChunkNoiseSamplerFlatCache implements IFastCacheLike {

    @Shadow @Final private ChunkNoiseSampler field_36611;

    @Shadow @Final private double[] cache;

    @Mutable
    @Shadow @Final private DensityFunction delegate;

    @Shadow @Final private int horizontalCacheSize;

    @Override
    public double c2me$getCached(int x, int y, int z, EvalType evalType) {
        int i = BiomeCoords.fromBlock(x);
        int j = BiomeCoords.fromBlock(z);
        int k = i - ((IChunkNoiseSampler) this.field_36611).getStartBiomeX();
        int l = j - ((IChunkNoiseSampler) this.field_36611).getStartBiomeZ();
        int m = this.horizontalCacheSize;
        if (k >= 0 && l >= 0 && k < m && l < m) {
            return this.cache[k + l * this.horizontalCacheSize];
        } else {
            return Double.longBitsToDouble(CACHE_MISS_NAN_BITS);
        }
    }

    @Override
    public boolean c2me$getCached(double[] res, int[] x, int[] y, int[] z, EvalType evalType) {
        for (int i = 0; i < res.length; i ++) {
            int i1 = BiomeCoords.fromBlock(x[i]);
            int j1 = BiomeCoords.fromBlock(z[i]);
            int k = i1 - ((IChunkNoiseSampler) this.field_36611).getStartBiomeX();
            int l = j1 - ((IChunkNoiseSampler) this.field_36611).getStartBiomeZ();
            int m = this.horizontalCacheSize;
            if (k >= 0 && l >= 0 && k < m && l < m) {
                res[i] = this.cache[k + l * this.horizontalCacheSize];
            } else {
                System.out.println("partial flat cache hit");
                return false; // partial hit possible
            }
        }
        return true;
    }

    @Override
    public void c2me$cache(int x, int y, int z, EvalType evalType, double cached) {
        // nop
    }

    @Override
    public void c2me$cache(double[] res, int[] x, int[] y, int[] z, EvalType evalType) {
        // nop
    }

    @Override
    public boolean c2me$isActualCache() {
        return true;
    }

    @Override
    public String c2me$describeCacheLike() {
        return "flat_cache";
    }

    @Override
    public DensityFunction c2me$getDelegate() {
        return this.delegate;
    }

    @Override
    public DensityFunction c2me$withDelegate(DensityFunction delegate) {
        this.delegate = delegate;
        return this;
    }
}
