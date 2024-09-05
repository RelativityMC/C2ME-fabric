package com.ishland.c2me.base.mixin.access;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OctavePerlinNoiseSampler.class)
public interface IOctavePerlinNoiseSampler {

    @Accessor
    PerlinNoiseSampler[] getOctaveSamplers();

    @Accessor
    DoubleList getAmplitudes();

    @Accessor
    double getPersistence();

    @Accessor
    double getLacunarity();

}
