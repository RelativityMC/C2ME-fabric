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
