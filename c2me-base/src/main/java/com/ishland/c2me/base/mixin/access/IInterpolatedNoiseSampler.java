package com.ishland.c2me.base.mixin.access;

import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InterpolatedNoiseSampler.class)
public interface IInterpolatedNoiseSampler {

    @Accessor
    OctavePerlinNoiseSampler getLowerInterpolatedNoise();

    @Accessor
    OctavePerlinNoiseSampler getUpperInterpolatedNoise();

    @Accessor
    OctavePerlinNoiseSampler getInterpolationNoise();

    @Accessor
    double getScaledXzScale();

    @Accessor
    double getScaledYScale();

    @Accessor
    double getXzFactor();

    @Accessor
    double getYFactor();

    @Accessor
    double getSmearScaleMultiplier();

    @Accessor
    double getMaxValue();

    @Accessor
    double getXzScale();

    @Accessor
    double getYScale();

}
