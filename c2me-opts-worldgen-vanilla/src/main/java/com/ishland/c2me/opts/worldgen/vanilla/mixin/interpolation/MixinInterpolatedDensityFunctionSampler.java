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

package com.ishland.c2me.opts.worldgen.vanilla.mixin.interpolation;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SamplingRegion;
import net.minecraft.world.gen.densityfunction.InterpolatedDensityFunction;
import net.minecraft.world.gen.sampler.SampleBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(InterpolatedDensityFunction.Sampler.class)
public abstract class MixinInterpolatedDensityFunctionSampler {

    @Shadow
    public abstract int cellSizeXz();

    @Shadow
    public abstract int cellSizeY();

    @Shadow
    public abstract float cellSizeXzInv();

    @Shadow
    public abstract float cellSizeYInv();

    @Unique
    private static void c2me$ramp(final SampleBuffer output, final int index, final float value, final float valueStep, final int count) {
        float v = value;
        for (int y = 0; y < count; y++) {
            output.set(index + y, v);
            v += valueStep;
        }
    }

    /**
     * @author ishland
     * @reason special case common cell height
     */
    @Overwrite
    private void interpolate(
            final SampleBuffer output,
            final SamplingRegion outputRegion,
            final SamplingRegion inputRegion,
            final int cellX,
            final int cellY,
            final int cellZ,
            final float a000,
            final float a100,
            final float a010,
            final float a110,
            final float a001,
            final float a101,
            final float a011,
            final float a111
    ) {
        int cellSizeXz = this.cellSizeXz();
        int cellSizeY = this.cellSizeY();
        float cellSizeXzInv = this.cellSizeXzInv();
        float cellSizeYInv = this.cellSizeYInv();
        int cellOutputX = inputRegion.translateX(cellX) - outputRegion.minBlockX();
        int cellOutputY = inputRegion.translateY(cellY) - outputRegion.minBlockY();
        int cellOutputZ = inputRegion.translateZ(cellZ) - outputRegion.minBlockZ();
        int x0 = Math.max(0, -cellOutputX);
        int y0 = Math.max(0, -cellOutputY);
        int z0 = Math.max(0, -cellOutputZ);
        int x1 = Math.min(cellSizeXz, outputRegion.sizeX() - cellOutputX) - 1;
        int y1 = Math.min(cellSizeY, outputRegion.sizeY() - cellOutputY) - 1;
        int z1 = Math.min(cellSizeXz, outputRegion.sizeZ() - cellOutputZ) - 1;

        if (x0 == 0 && y0 == 0 && z0 == 0 && x1 == cellSizeXz - 1 && y1 == cellSizeY - 1 && z1 == cellSizeXz - 1) {
            if (cellSizeY == 8) {
                c2me$fillCells8(output, outputRegion, cellOutputX, cellOutputY, cellOutputZ,
                        cellSizeXz, cellSizeXzInv, cellSizeYInv,
                        a000, a100, a010, a110, a001, a101, a011, a111);
            } else if (cellSizeY == 4) {
                c2me$fillCells4(output, outputRegion, cellOutputX, cellOutputY, cellOutputZ,
                        cellSizeXz, cellSizeXzInv, cellSizeYInv,
                        a000, a100, a010, a110, a001, a101, a011, a111);
            } else if (cellSizeY == 2) {
                c2me$fillCells2(output, outputRegion, cellOutputX, cellOutputY, cellOutputZ,
                        cellSizeXz, cellSizeXzInv, cellSizeYInv,
                        a000, a100, a010, a110, a001, a101, a011, a111);
            } else {
                c2me$fillCells(output, outputRegion, cellOutputX, cellOutputY, cellOutputZ,
                        cellSizeXz, cellSizeXzInv, cellSizeYInv, cellSizeY,
                        a000, a100, a010, a110, a001, a101, a011, a111);
            }
            return;
        }

        for (int z = z0; z <= z1; z++) {
            int outputZ = cellOutputZ + z;
            float alphaZ = z * cellSizeXzInv;
            float v00_ = MathHelper.lerp(alphaZ, a000, a001);
            float v01_ = MathHelper.lerp(alphaZ, a010, a011);
            float v10_ = MathHelper.lerp(alphaZ, a100, a101);
            float v11_ = MathHelper.lerp(alphaZ, a110, a111);

            for (int x = x0; x <= x1; x++) {
                int outputX = cellOutputX + x;
                float alphaX = x * cellSizeXzInv;
                float v_0_ = MathHelper.lerp(alphaX, v00_, v10_);
                float v_1_ = MathHelper.lerp(alphaX, v01_, v11_);
                float valueStep = (v_1_ - v_0_) * cellSizeYInv;
                float value = v_0_ + valueStep * y0;
                int outputIndex = outputRegion.getIndex(outputX, cellOutputY + y0, outputZ);

                for (int y = y0; y <= y1; y++) {
                    output.set(outputIndex++, value);
                    value += valueStep;
                }
            }
        }
    }

    @Unique
    private static void c2me$fillCells8(
            final SampleBuffer output, final SamplingRegion outputRegion,
            final int cellOutputX, final int cellOutputY, final int cellOutputZ,
            final int cellSizeXz, final float cellSizeXzInv, final float cellSizeYInv,
            final float a000, final float a100, final float a010, final float a110,
            final float a001, final float a101, final float a011, final float a111
    ) {
        for (int z = 0; z < cellSizeXz; z++) {
            int outputZ = cellOutputZ + z;
            float alphaZ = z * cellSizeXzInv;
            float v00_ = MathHelper.lerp(alphaZ, a000, a001);
            float v01_ = MathHelper.lerp(alphaZ, a010, a011);
            float v10_ = MathHelper.lerp(alphaZ, a100, a101);
            float v11_ = MathHelper.lerp(alphaZ, a110, a111);

            for (int x = 0; x < cellSizeXz; x++) {
                float alphaX = x * cellSizeXzInv;
                float v_0_ = MathHelper.lerp(alphaX, v00_, v10_);
                float v_1_ = MathHelper.lerp(alphaX, v01_, v11_);
                float valueStep = (v_1_ - v_0_) * cellSizeYInv;
                float value = v_0_ + valueStep * 0.0F;
                int outputIndex = outputRegion.getIndex(cellOutputX + x, cellOutputY, outputZ);
                c2me$ramp(output, outputIndex, value, valueStep, 8);
            }
        }
    }

    @Unique
    private static void c2me$fillCells4(
            final SampleBuffer output, final SamplingRegion outputRegion,
            final int cellOutputX, final int cellOutputY, final int cellOutputZ,
            final int cellSizeXz, final float cellSizeXzInv, final float cellSizeYInv,
            final float a000, final float a100, final float a010, final float a110,
            final float a001, final float a101, final float a011, final float a111
    ) {
        for (int z = 0; z < cellSizeXz; z++) {
            int outputZ = cellOutputZ + z;
            float alphaZ = z * cellSizeXzInv;
            float v00_ = MathHelper.lerp(alphaZ, a000, a001);
            float v01_ = MathHelper.lerp(alphaZ, a010, a011);
            float v10_ = MathHelper.lerp(alphaZ, a100, a101);
            float v11_ = MathHelper.lerp(alphaZ, a110, a111);

            for (int x = 0; x < cellSizeXz; x++) {
                float alphaX = x * cellSizeXzInv;
                float v_0_ = MathHelper.lerp(alphaX, v00_, v10_);
                float v_1_ = MathHelper.lerp(alphaX, v01_, v11_);
                float valueStep = (v_1_ - v_0_) * cellSizeYInv;
                float value = v_0_ + valueStep * 0.0F;
                int outputIndex = outputRegion.getIndex(cellOutputX + x, cellOutputY, outputZ);
                c2me$ramp(output, outputIndex, value, valueStep, 4);
            }
        }
    }

    @Unique
    private static void c2me$fillCells2(
            final SampleBuffer output, final SamplingRegion outputRegion,
            final int cellOutputX, final int cellOutputY, final int cellOutputZ,
            final int cellSizeXz, final float cellSizeXzInv, final float cellSizeYInv,
            final float a000, final float a100, final float a010, final float a110,
            final float a001, final float a101, final float a011, final float a111
    ) {
        for (int z = 0; z < cellSizeXz; z++) {
            int outputZ = cellOutputZ + z;
            float alphaZ = z * cellSizeXzInv;
            float v00_ = MathHelper.lerp(alphaZ, a000, a001);
            float v01_ = MathHelper.lerp(alphaZ, a010, a011);
            float v10_ = MathHelper.lerp(alphaZ, a100, a101);
            float v11_ = MathHelper.lerp(alphaZ, a110, a111);

            for (int x = 0; x < cellSizeXz; x++) {
                float alphaX = x * cellSizeXzInv;
                float v_0_ = MathHelper.lerp(alphaX, v00_, v10_);
                float v_1_ = MathHelper.lerp(alphaX, v01_, v11_);
                float valueStep = (v_1_ - v_0_) * cellSizeYInv;
                float value = v_0_ + valueStep * 0.0F;
                int outputIndex = outputRegion.getIndex(cellOutputX + x, cellOutputY, outputZ);
                c2me$ramp(output, outputIndex, value, valueStep, 2);
            }
        }
    }

    @Unique
    private static void c2me$fillCells(
            final SampleBuffer output, final SamplingRegion outputRegion,
            final int cellOutputX, final int cellOutputY, final int cellOutputZ,
            final int cellSizeXz, final float cellSizeXzInv, final float cellSizeYInv, final int cellSizeY,
            final float a000, final float a100, final float a010, final float a110,
            final float a001, final float a101, final float a011, final float a111
    ) {
        for (int z = 0; z < cellSizeXz; z++) {
            int outputZ = cellOutputZ + z;
            float alphaZ = z * cellSizeXzInv;
            float v00_ = MathHelper.lerp(alphaZ, a000, a001);
            float v01_ = MathHelper.lerp(alphaZ, a010, a011);
            float v10_ = MathHelper.lerp(alphaZ, a100, a101);
            float v11_ = MathHelper.lerp(alphaZ, a110, a111);

            for (int x = 0; x < cellSizeXz; x++) {
                float alphaX = x * cellSizeXzInv;
                float v_0_ = MathHelper.lerp(alphaX, v00_, v10_);
                float v_1_ = MathHelper.lerp(alphaX, v01_, v11_);
                float valueStep = (v_1_ - v_0_) * cellSizeYInv;
                float value = v_0_ + valueStep * 0.0F;
                int outputIndex = outputRegion.getIndex(cellOutputX + x, cellOutputY, outputZ);
                c2me$ramp(output, outputIndex, value, valueStep, cellSizeY);
            }
        }
    }

}
