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
import com.ishland.c2me.opts.dfc.common.gen.jvm.vif.NoisePosVanillaInterface;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkNoiseSampler.CellCache.class)
public abstract class MixinChunkNoiseSamplerCellCache implements IFastCacheLike {

    @Shadow
    @Final
    private ChunkNoiseSampler field_36602;

    @Shadow
    @Final
    private double[] cache;

    @Mutable
    @Shadow
    @Final
    private DensityFunction delegate;

    @WrapMethod(method = "sample")
    private double wrapSample(DensityFunction.NoisePos pos, Operation<Double> original) {
        if (pos instanceof ChunkNoiseSampler) {
            return original.call(pos);
        }
        if (pos instanceof NoisePosVanillaInterface vif && vif.getType() == EvalType.INTERPOLATION) {
            boolean isInInterpolationLoop = ((IChunkNoiseSampler) this.field_36602).getIsInInterpolationLoop();
            if (!isInInterpolationLoop) {
                return original.call(pos);
            }
            int startBlockX = ((IChunkNoiseSampler) this.field_36602).getStartBlockX();
            int startBlockY = ((IChunkNoiseSampler) this.field_36602).getStartBlockY();
            int startBlockZ = ((IChunkNoiseSampler) this.field_36602).getStartBlockZ();
            int horizontalCellBlockCount = ((IChunkNoiseSampler) this.field_36602).getHorizontalCellBlockCount();
            int verticalCellBlockCount = ((IChunkNoiseSampler) this.field_36602).getVerticalCellBlockCount();
            int cellBlockX = pos.blockX() - startBlockX;
            int cellBlockY = pos.blockY() - startBlockY;
            int cellBlockZ = pos.blockZ() - startBlockZ;
            return cellBlockX >= 0
                    && cellBlockY >= 0
                    && cellBlockZ >= 0
                    && cellBlockX < horizontalCellBlockCount
                    && cellBlockY < verticalCellBlockCount
                    && cellBlockZ < horizontalCellBlockCount
                    ? this.cache[((verticalCellBlockCount - 1 - cellBlockY) * horizontalCellBlockCount + cellBlockX) * horizontalCellBlockCount + cellBlockZ]
                    : this.delegate.sample(pos);
        }
        return original.call(pos);
    }

    @Override
    public double c2me$getCached(int x, int y, int z, EvalType evalType) {
        if (evalType == EvalType.INTERPOLATION) {
            boolean isInInterpolationLoop = ((IChunkNoiseSampler) this.field_36602).getIsInInterpolationLoop();
            if (isInInterpolationLoop) {
                int startBlockX = ((IChunkNoiseSampler) this.field_36602).getStartBlockX();
                int startBlockY = ((IChunkNoiseSampler) this.field_36602).getStartBlockY();
                int startBlockZ = ((IChunkNoiseSampler) this.field_36602).getStartBlockZ();
                int horizontalCellBlockCount = ((IChunkNoiseSampler) this.field_36602).getHorizontalCellBlockCount();
                int verticalCellBlockCount = ((IChunkNoiseSampler) this.field_36602).getVerticalCellBlockCount();
                int cellBlockX = x - startBlockX;
                int cellBlockY = y - startBlockY;
                int cellBlockZ = z - startBlockZ;
                if (cellBlockX >= 0 &&
                        cellBlockY >= 0 &&
                        cellBlockZ >= 0 &&
                        cellBlockX < horizontalCellBlockCount &&
                        cellBlockY < verticalCellBlockCount &&
                        cellBlockZ < horizontalCellBlockCount) {
                    return this.cache[((verticalCellBlockCount - 1 - cellBlockY) * horizontalCellBlockCount + cellBlockX)
                            * horizontalCellBlockCount
                            + cellBlockZ];
                }
            }
        }

        return Double.longBitsToDouble(CACHE_MISS_NAN_BITS);
    }

    @Override
    public boolean c2me$getCached(double[] res, int[] x, int[] y, int[] z, EvalType evalType) {
        if (evalType == EvalType.INTERPOLATION) {
            boolean isInInterpolationLoop = ((IChunkNoiseSampler) this.field_36602).getIsInInterpolationLoop();
            if (isInInterpolationLoop) {
                int startBlockX = ((IChunkNoiseSampler) this.field_36602).getStartBlockX();
                int startBlockY = ((IChunkNoiseSampler) this.field_36602).getStartBlockY();
                int startBlockZ = ((IChunkNoiseSampler) this.field_36602).getStartBlockZ();
                int horizontalCellBlockCount = ((IChunkNoiseSampler) this.field_36602).getHorizontalCellBlockCount();
                int verticalCellBlockCount = ((IChunkNoiseSampler) this.field_36602).getVerticalCellBlockCount();
                for (int i = 0; i < res.length; i++) {
                    int cellBlockX = x[i] - startBlockX;
                    int cellBlockY = y[i] - startBlockY;
                    int cellBlockZ = z[i] - startBlockZ;
                    if (cellBlockX >= 0 &&
                            cellBlockY >= 0 &&
                            cellBlockZ >= 0 &&
                            cellBlockX < horizontalCellBlockCount &&
                            cellBlockY < verticalCellBlockCount &&
                            cellBlockZ < horizontalCellBlockCount) {
                        res[i] = this.cache[((verticalCellBlockCount - 1 - cellBlockY) * horizontalCellBlockCount + cellBlockX) * horizontalCellBlockCount + cellBlockZ];
                    } else {
                        System.out.println("partial cell cache hit");
                        return false; // partial hit possible
                    }
                }
            }
        }

        return false;
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
        return "cache_all_in_cell";
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
