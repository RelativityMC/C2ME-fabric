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

package com.ishland.c2me.base.mixin.access;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkNoiseSampler.class)
public interface IChunkNoiseSampler {

    @Accessor
    int getStartBlockX();

    @Accessor
    int getStartBlockY();

    @Accessor
    int getStartBlockZ();

    @Accessor
    int getHorizontalCellBlockCount();

    @Accessor
    int getVerticalCellBlockCount();

    @Accessor
    boolean getIsInInterpolationLoop();

    @Accessor
    boolean getIsSamplingForCaches();

    @Accessor
    int getStartBiomeX();

    @Accessor
    int getStartBiomeZ();

    @Accessor
    int getHorizontalCellCount();

    @Accessor
    int getVerticalCellCount();

    @Accessor
    int getMinimumCellY();

    @Accessor
    int getCellBlockX();

    @Accessor
    int getCellBlockY();

    @Accessor
    int getCellBlockZ();

    @Invoker
    BlockState invokeSampleBlockState();

    @Accessor
    int getHorizontalBiomeEnd();

    @Accessor
    DensityFunctionTypes.Beardifying getBeardifying();

    @Accessor
    int getStartCellX();

    @Accessor
    int getStartCellZ();

    @Accessor
    Blender getBlender();

}
