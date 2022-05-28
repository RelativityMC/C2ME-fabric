package noise;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;

import java.util.stream.IntStream;

public class InterpolatedNoiseSamplerCopy {
    private final OctavePerlinNoiseSampler lowerInterpolatedNoise;
    private final OctavePerlinNoiseSampler upperInterpolatedNoise;
    private final OctavePerlinNoiseSampler interpolationNoise;
    private final double field_38271;
    private final double field_38272;
    private final double xzFactor;
    private final double yFactor;
    private final double smearScaleMultiplier;
    private final double maxValue;
    private final double xzScale;
    private final double yScale;

    public static InterpolatedNoiseSamplerCopy createBase3dNoiseFunction(double xzScale, double yScale, double xzFactor, double yFactor, double smearScaleMultiplier) {
        return new InterpolatedNoiseSamplerCopy(new Xoroshiro128PlusPlusRandom(0L), xzScale, yScale, xzFactor, yFactor, smearScaleMultiplier);
    }

    private InterpolatedNoiseSamplerCopy(
            OctavePerlinNoiseSampler lowerInterpolatedNoise,
            OctavePerlinNoiseSampler upperInterpolatedNoise,
            OctavePerlinNoiseSampler interpolationNoise,
            double xzScale,
            double yScale,
            double xzFactor,
            double yFactor,
            double smearScaleMultiplier
    ) {
        this.lowerInterpolatedNoise = lowerInterpolatedNoise;
        this.upperInterpolatedNoise = upperInterpolatedNoise;
        this.interpolationNoise = interpolationNoise;
        this.xzScale = xzScale;
        this.yScale = yScale;
        this.xzFactor = xzFactor;
        this.yFactor = yFactor;
        this.smearScaleMultiplier = smearScaleMultiplier;
        this.field_38271 = 684.412 * this.xzScale;
        this.field_38272 = 684.412 * this.yScale;
        this.maxValue = lowerInterpolatedNoise.method_40556(this.field_38272);
    }

    @VisibleForTesting
    public InterpolatedNoiseSamplerCopy(Random random, double xzScale, double yScale, double xzFactor, double yFactor, double smearScaleMultiplier) {
        this(
                OctavePerlinNoiseSampler.createLegacy(random, IntStream.rangeClosed(-15, 0)),
                OctavePerlinNoiseSampler.createLegacy(random, IntStream.rangeClosed(-15, 0)),
                OctavePerlinNoiseSampler.createLegacy(random, IntStream.rangeClosed(-7, 0)),
                xzScale,
                yScale,
                xzFactor,
                yFactor,
                smearScaleMultiplier
        );
    }

    public InterpolatedNoiseSampler copyWithRandom(Random random) {
        return new InterpolatedNoiseSampler(random, this.xzScale, this.yScale, this.xzFactor, this.yFactor, this.smearScaleMultiplier);
    }

    public double sample(NoisePos pos) {
        double d = (double)pos.blockX() * this.field_38271;
        double e = (double)pos.blockY() * this.field_38272;
        double f = (double)pos.blockZ() * this.field_38271;
        double g = d / this.xzFactor;
        double h = e / this.yFactor;
        double i = f / this.xzFactor;
        double j = this.field_38272 * this.smearScaleMultiplier;
        double k = j / this.yFactor;
        double l = 0.0;
        double m = 0.0;
        double n = 0.0;
        boolean bl = true;
        double o = 1.0;

        for(int p = 0; p < 8; ++p) {
            PerlinNoiseSampler perlinNoiseSampler = this.interpolationNoise.getOctave(p);
            if (perlinNoiseSampler != null) {
                n += perlinNoiseSampler.sample(
                        OctavePerlinNoiseSampler.maintainPrecision(g * o),
                        OctavePerlinNoiseSampler.maintainPrecision(h * o),
                        OctavePerlinNoiseSampler.maintainPrecision(i * o),
                        k * o,
                        h * o
                )
                        / o;
            }

            o /= 2.0;
        }

        double q = (n / 10.0 + 1.0) / 2.0;
        boolean bl2 = q >= 1.0;
        boolean bl3 = q <= 0.0;
        o = 1.0;

        for(int r = 0; r < 16; ++r) {
            double s = OctavePerlinNoiseSampler.maintainPrecision(d * o);
            double t = OctavePerlinNoiseSampler.maintainPrecision(e * o);
            double u = OctavePerlinNoiseSampler.maintainPrecision(f * o);
            double v = j * o;
            if (!bl2) {
                PerlinNoiseSampler perlinNoiseSampler2 = this.lowerInterpolatedNoise.getOctave(r);
                if (perlinNoiseSampler2 != null) {
                    l += perlinNoiseSampler2.sample(s, t, u, v, e * o) / o;
                }
            }

            if (!bl3) {
                PerlinNoiseSampler perlinNoiseSampler2 = this.upperInterpolatedNoise.getOctave(r);
                if (perlinNoiseSampler2 != null) {
                    m += perlinNoiseSampler2.sample(s, t, u, v, e * o) / o;
                }
            }

            o /= 2.0;
        }

        return MathHelper.clampedLerp(l / 512.0, m / 512.0, q) / 128.0;
    }

    record NoisePos(int blockX, int blockY, int blockZ) {
    }

}
