package noise;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.world.gen.chunk.NoiseSamplingConfig;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.random.AbstractRandom;
import net.minecraft.world.gen.random.Xoroshiro128PlusPlusRandom;

import java.util.stream.IntStream;

public class InterpolatedNoiseSamplerCopy {
    public static final InterpolatedNoiseSamplerCopy field_37205 = new InterpolatedNoiseSamplerCopy(
            new Xoroshiro128PlusPlusRandom(0L), new NoiseSamplingConfig(1.0, 1.0, 80.0, 160.0), 4, 8
    );
    private final OctavePerlinNoiseSampler lowerInterpolatedNoise;
    private final OctavePerlinNoiseSampler upperInterpolatedNoise;
    private final OctavePerlinNoiseSampler interpolationNoise;
    private final double xzScale;
    private final double yScale;
    private final double xzMainScale;
    private final double yMainScale;
    private final int cellWidth;
    private final int cellHeight;
    private final double field_36630;

    private InterpolatedNoiseSamplerCopy(
            OctavePerlinNoiseSampler lowerInterpolatedNoise,
            OctavePerlinNoiseSampler upperInterpolatedNoise,
            OctavePerlinNoiseSampler interpolationNoise,
            NoiseSamplingConfig config,
            int cellWidth,
            int cellHeight
    ) {
        this.lowerInterpolatedNoise = lowerInterpolatedNoise;
        this.upperInterpolatedNoise = upperInterpolatedNoise;
        this.interpolationNoise = interpolationNoise;
        this.xzScale = 684.412 * config.xzScale();
        this.yScale = 684.412 * config.yScale();
        this.xzMainScale = this.xzScale / config.xzFactor();
        this.yMainScale = this.yScale / config.yFactor();
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.field_36630 = lowerInterpolatedNoise.method_40556(this.yScale);
    }

    public InterpolatedNoiseSamplerCopy(AbstractRandom random, NoiseSamplingConfig config, int cellWidth, int cellHeight) {
        this(
                OctavePerlinNoiseSampler.createLegacy(random, IntStream.rangeClosed(-15, 0)),
                OctavePerlinNoiseSampler.createLegacy(random, IntStream.rangeClosed(-15, 0)),
                OctavePerlinNoiseSampler.createLegacy(random, IntStream.rangeClosed(-7, 0)),
                config,
                cellWidth,
                cellHeight
        );
    }

    public double sample(DensityFunction.NoisePos pos) {
        int i = Math.floorDiv(pos.blockX(), this.cellWidth);
        int j = Math.floorDiv(pos.blockY(), this.cellHeight);
        int k = Math.floorDiv(pos.blockZ(), this.cellWidth);
        double d = 0.0;
        double e = 0.0;
        double f = 0.0;
        boolean bl = true;
        double g = 1.0;

        for(int l = 0; l < 8; ++l) {
            PerlinNoiseSampler perlinNoiseSampler = this.interpolationNoise.getOctave(l);
            if (perlinNoiseSampler != null) {
                f += perlinNoiseSampler.sample(
                        OctavePerlinNoiseSampler.maintainPrecision((double)i * this.xzMainScale * g),
                        OctavePerlinNoiseSampler.maintainPrecision((double)j * this.yMainScale * g),
                        OctavePerlinNoiseSampler.maintainPrecision((double)k * this.xzMainScale * g),
                        this.yMainScale * g,
                        (double)j * this.yMainScale * g
                )
                        / g;
            }

            g /= 2.0;
        }

        double h = (f / 10.0 + 1.0) / 2.0;
        boolean bl2 = h >= 1.0;
        boolean bl3 = h <= 0.0;
        g = 1.0;

        for(int m = 0; m < 16; ++m) {
            double n = OctavePerlinNoiseSampler.maintainPrecision((double)i * this.xzScale * g);
            double o = OctavePerlinNoiseSampler.maintainPrecision((double)j * this.yScale * g);
            double p = OctavePerlinNoiseSampler.maintainPrecision((double)k * this.xzScale * g);
            double q = this.yScale * g;
            if (!bl2) {
                PerlinNoiseSampler perlinNoiseSampler2 = this.lowerInterpolatedNoise.getOctave(m);
                if (perlinNoiseSampler2 != null) {
                    d += perlinNoiseSampler2.sample(n, o, p, q, (double)j * q) / g;
                }
            }

            if (!bl3) {
                PerlinNoiseSampler perlinNoiseSampler2 = this.upperInterpolatedNoise.getOctave(m);
                if (perlinNoiseSampler2 != null) {
                    e += perlinNoiseSampler2.sample(n, o, p, q, (double)j * q) / g;
                }
            }

            g /= 2.0;
        }

        return MathHelper.clampedLerp(d / 512.0, e / 512.0, h) / 128.0;
    }
}
