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
