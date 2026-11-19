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

package com.ishland.c2me.opts.dfc.mixin.equality;

import net.minecraft.util.math.noise.PerlinNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;

@Mixin(PerlinNoiseSampler.class)
public class MixinPerlinNoiseSampler {

    @Shadow @Final private byte[] permutation;

    @Shadow @Final public double originX;

    @Shadow @Final public double originY;

    @Shadow @Final public double originZ;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MixinPerlinNoiseSampler that = (MixinPerlinNoiseSampler) object;
        return Double.compare(originX, that.originX) == 0 && Double.compare(originY, that.originY) == 0 && Double.compare(originZ, that.originZ) == 0 && Arrays.equals(permutation, that.permutation);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Arrays.hashCode(permutation);
        result = 31 * result + Double.hashCode(originX);
        result = 31 * result + Double.hashCode(originY);
        result = 31 * result + Double.hashCode(originZ);
        return result;
    }
}
