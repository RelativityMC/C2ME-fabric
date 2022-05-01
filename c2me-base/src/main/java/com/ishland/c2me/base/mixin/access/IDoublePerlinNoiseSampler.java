package com.ishland.c2me.base.mixin.access;

import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DoublePerlinNoiseSampler.class)
public interface IDoublePerlinNoiseSampler {

    @Accessor
    double getAmplitude();

    @Accessor
    OctavePerlinNoiseSampler getFirstSampler();

    @Accessor
    OctavePerlinNoiseSampler getSecondSampler();

}
