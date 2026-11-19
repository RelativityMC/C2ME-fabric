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

import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(DoublePerlinNoiseSampler.class)
public class MixinDoublePerlinNoiseSampler {

    @Shadow @Final private double amplitude;

    @Shadow @Final private OctavePerlinNoiseSampler firstSampler;

    @Shadow @Final private OctavePerlinNoiseSampler secondSampler;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MixinDoublePerlinNoiseSampler that = (MixinDoublePerlinNoiseSampler) object;
        return Double.compare(amplitude, that.amplitude) == 0 && Objects.equals(firstSampler, that.firstSampler) && Objects.equals(secondSampler, that.secondSampler);
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + Double.hashCode(amplitude);
        result = 31 * result + Objects.hashCode(firstSampler);
        result = 31 * result + Objects.hashCode(secondSampler);

        return result;
    }
}
