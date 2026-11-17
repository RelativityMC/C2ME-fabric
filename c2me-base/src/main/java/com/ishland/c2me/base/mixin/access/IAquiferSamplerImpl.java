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

import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.gen.chunk.AquiferSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AquiferSampler.Impl.class)
public interface IAquiferSamplerImpl {

    @Accessor("startCellX")
    int getStartX();

    @Accessor("startCellY")
    int getStartY();

    @Accessor("startCellZ")
    int getStartZ();

    @Accessor("cellCountX")
    int getSizeX();

    @Accessor("cellCountZ")
    int getSizeZ();

    @Accessor
    long[] getBlockPositions();

    @Accessor
    RandomSplitter getRandomDeriver();

    @Accessor
    AquiferSampler.FluidLevelSampler getFluidLevelSampler();

    @Accessor(value = "maxY")
    int getSamplingYLowPassCutoff();

    @Invoker("getCellX")
    static int invokeGetLocalX(int i) {
        throw new AbstractMethodError();
    }

    @Invoker("getCellY")
    static int invokeGetLocalY(int i) {
        throw new AbstractMethodError();
    }

    @Invoker("getCellZ")
    static int invokeGetLocalZ(int i) {
        throw new AbstractMethodError();
    }

}
