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

package com.ishland.c2me.opts.natives_math.mixin;

import com.ishland.c2me.base.mixin.access.ICompositeNoiseSampler;
import net.minecraft.util.math.noise.CompositeNoiseSampler;
import net.minecraft.util.math.noise.NoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(CompositeNoiseSampler.Builder.class)
public class MixinCompositeNoiseSamplerBuilder {

    @Shadow
    @Final
    private List<CompositeNoiseSampler.Component> components;

    /**
     * @author ishland
     * @reason flattening
     */
    @Overwrite
    public CompositeNoiseSampler.Builder component(final NoiseSampler noise, final double frequency, final float amplitude) {
        if (noise instanceof CompositeNoiseSampler nested) {
            this.components(nested, frequency, amplitude);
        } else {
            this.components.add(new CompositeNoiseSampler.Component(noise, frequency, amplitude));
        }

        return (CompositeNoiseSampler.Builder) (Object) this;
    }

    /**
     * @author ishland
     * @reason flattening
     */
    @Overwrite
    public CompositeNoiseSampler.Builder components(final CompositeNoiseSampler compositeNoise, final double frequency, final float amplitude) {
        for (CompositeNoiseSampler.Component layer : ((ICompositeNoiseSampler) compositeNoise).getComponents()) {
            this.component(layer.noise(), layer.frequency() * frequency, layer.amplitude() * amplitude);
        }

        return (CompositeNoiseSampler.Builder) (Object) this;
    }

}
