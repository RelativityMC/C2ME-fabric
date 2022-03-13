package com.ishland.c2me.opts.math.mixin;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OctavePerlinNoiseSampler.class)
public class MixinOctavePerlinNoiseSampler {

    @Shadow @Final private double lacunarity;

    @Shadow @Final private double persistence;

    @Shadow @Final private PerlinNoiseSampler[] octaveSamplers;

    @Shadow @Final private DoubleList amplitudes;

    @Unique
    private int octaveSamplersCount = 0;

    @Unique
    private double[] amplitudesArray = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.octaveSamplersCount = this.octaveSamplers.length;
        this.amplitudesArray = this.amplitudes.toDoubleArray();
    }

    /**
     * @author ishland
     * @reason remove frequent type conversion
     */
    @Overwrite
    public static double maintainPrecision(double value) {
        return value - Math.floor(value / 3.3554432E7 + 0.5) * 3.3554432E7;
    }

    /**
     * @author ishland
     * @reason optimize for common cases
     */
    @Overwrite
    public double sample(double x, double y, double z) {
        double d = 0.0;
        double e = this.lacunarity;
        double f = this.persistence;

        for(int i = 0; i < this.octaveSamplersCount; ++i) {
            PerlinNoiseSampler perlinNoiseSampler = this.octaveSamplers[i];
            if (perlinNoiseSampler != null) {
                @SuppressWarnings("deprecation")
                double g = perlinNoiseSampler.sample(
                        maintainPrecision(x * e), maintainPrecision(y * e), maintainPrecision(z * e), 0.0, 0.0
                );
                d += this.amplitudesArray[i] * g * f;
            }

            e *= 2.0;
            f /= 2.0;
        }

        return d;
    }

}
