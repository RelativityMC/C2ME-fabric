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

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(DensityFunction.Noise.class)
public class MixinDensityFunctionNoise {

    @Shadow @Final private @Nullable DoublePerlinNoiseSampler noise;

    @Shadow @Final private RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> noiseData;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MixinDensityFunctionNoise that = (MixinDensityFunctionNoise) object;
        return (this.noise != null || that.noise != null) ? Objects.equals(noise, that.noise) : Objects.equals(noiseData, that.noiseData);
    }

    @Override
    public int hashCode() {
        if (this.noise != null) {
            return this.noise.hashCode();
        } else {
            return Objects.hashCode(this.noiseData);
        }
    }

}
