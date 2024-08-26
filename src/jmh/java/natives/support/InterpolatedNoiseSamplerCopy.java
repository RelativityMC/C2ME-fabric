package natives.support;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;

public class InterpolatedNoiseSamplerCopy {

    public static MemorySegment interpolated_noise_sampler$create(InterpolatedNoiseSamplerCopy interpolated) {
        final Arena arena = Arena.ofShared(); // this is fine
        final MemorySegment data = arena.allocate(BindingsTemplate.interpolated_noise_sampler.byteSize(), 64);
        BindingsTemplate.interpolated_noise_sampler$scaledXzScale.set(data, 0L, ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "scaledXzScale"));
        BindingsTemplate.interpolated_noise_sampler$scaledYScale.set(data, 0L, ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "scaledYScale"));
        BindingsTemplate.interpolated_noise_sampler$xzFactor.set(data, 0L, ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "xzFactor"));
        BindingsTemplate.interpolated_noise_sampler$yFactor.set(data, 0L, ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "yFactor"));
        BindingsTemplate.interpolated_noise_sampler$smearScaleMultiplier.set(data, 0L, ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "smearScaleMultiplier"));
        BindingsTemplate.interpolated_noise_sampler$xzScale.set(data, 0L, ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "xzScale"));
        BindingsTemplate.interpolated_noise_sampler$yScale.set(data, 0L, ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "yScale"));
        int countNonNull = Math.toIntExact(Stream.of(
                IntStream.range(0, 8).mapToObj(((OctavePerlinNoiseSampler) ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "interpolationNoise"))::getOctave),
                IntStream.range(0, 16).mapToObj(((OctavePerlinNoiseSampler) ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "lowerInterpolatedNoise"))::getOctave),
                IntStream.range(0, 16).mapToObj(((OctavePerlinNoiseSampler) ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "upperInterpolatedNoise"))::getOctave)
        ).flatMap(Function.identity()).filter(Objects::nonNull).count());

        System.out.println(String.format("Interpolated total: %d", countNonNull));
        System.out.println(String.format("lower: %d", IntStream.range(0, 16).mapToObj(((OctavePerlinNoiseSampler) ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "lowerInterpolatedNoise"))::getOctave).filter(Objects::nonNull).count()));
        System.out.println(String.format("upper: %d", IntStream.range(0, 16).mapToObj(((OctavePerlinNoiseSampler) ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "upperInterpolatedNoise"))::getOctave).filter(Objects::nonNull).count()));
        System.out.println(String.format("normal: %d", IntStream.range(0, 8).mapToObj(((OctavePerlinNoiseSampler) ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "interpolationNoise"))::getOctave).filter(Objects::nonNull).count()));

        final MemorySegment sampler_permutations = arena.allocate(countNonNull * 256L, 64);
        final MemorySegment sampler_originX = arena.allocate(countNonNull * 8L, 64);
        final MemorySegment sampler_originY = arena.allocate(countNonNull * 8L, 64);
        final MemorySegment sampler_originZ = arena.allocate(countNonNull * 8L, 64);
        final MemorySegment sampler_mulFactor = arena.allocate(countNonNull * 8L, 64);

        int index = 0;
        for (int i = 0; i < 16; i++) {
            PerlinNoiseSampler sampler = ((OctavePerlinNoiseSampler) ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "lowerInterpolatedNoise")).getOctave(i);
            if (sampler != null) {
                MemorySegment.copy(MemorySegment.ofArray((byte[]) ReflectUtils.getField(PerlinNoiseSampler.class, sampler, "permutation")), 0, sampler_permutations, index * 256L, 256);
                sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originX);
                sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originY);
                sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originZ);
                sampler_mulFactor.set(ValueLayout.JAVA_DOUBLE, index * 8L, Math.pow(2, -i));
                index++;
            }
        }

        BindingsTemplate.interpolated_noise_sampler$upperNoiseOffset.set(data, 0L, index);

        for (int i = 0; i < 16; i++) {
            PerlinNoiseSampler sampler = ((OctavePerlinNoiseSampler) ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "upperInterpolatedNoise")).getOctave(i);
            if (sampler != null) {
                MemorySegment.copy(MemorySegment.ofArray((byte[]) ReflectUtils.getField(PerlinNoiseSampler.class, sampler, "permutation")), 0, sampler_permutations, index * 256L, 256);
                sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originX);
                sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originY);
                sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originZ);
                sampler_mulFactor.set(ValueLayout.JAVA_DOUBLE, index * 8L, Math.pow(2, -i));
                index++;
            }
        }

        BindingsTemplate.interpolated_noise_sampler$normalNoiseOffset.set(data, 0L, index);

        for (int i = 0; i < 8; i++) {
            PerlinNoiseSampler sampler = ((OctavePerlinNoiseSampler) ReflectUtils.getField(InterpolatedNoiseSamplerCopy.class, interpolated, "interpolationNoise")).getOctave(i);
            if (sampler != null) {
                MemorySegment.copy(MemorySegment.ofArray((byte[]) ReflectUtils.getField(PerlinNoiseSampler.class, sampler, "permutation")), 0, sampler_permutations, index * 256L, 256);
                sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originX);
                sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originY);
                sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originZ);
                sampler_mulFactor.set(ValueLayout.JAVA_DOUBLE, index * 8L, Math.pow(2, -i));
                index++;
            }
        }

        BindingsTemplate.interpolated_noise_sampler$endOffset.set(data, 0L, index);

        BindingsTemplate.interpolated_noise_sampler$sampler_permutations.set(data, 0L, sampler_permutations);
        BindingsTemplate.interpolated_noise_sampler$sampler_originX.set(data, 0L, sampler_originX);
        BindingsTemplate.interpolated_noise_sampler$sampler_originY.set(data, 0L, sampler_originY);
        BindingsTemplate.interpolated_noise_sampler$sampler_originZ.set(data, 0L, sampler_originZ);
        BindingsTemplate.interpolated_noise_sampler$sampler_mulFactor.set(data, 0L, sampler_mulFactor);

        if (index != countNonNull) {
            throw new AssertionError("index != countNonNull");
        }

        VarHandle.fullFence();

        return data;
    }

    private final OctavePerlinNoiseSampler lowerInterpolatedNoise;
    private final OctavePerlinNoiseSampler upperInterpolatedNoise;
    private final OctavePerlinNoiseSampler interpolationNoise;
    private final double scaledXzScale;
    private final double scaledYScale;
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
        this.scaledXzScale = 684.412 * this.xzScale;
        this.scaledYScale = 684.412 * this.yScale;
        this.maxValue = lowerInterpolatedNoise.method_40556(this.scaledYScale);
    }

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

    public InterpolatedNoiseSamplerCopy copyWithRandom(Random random) {
        return new InterpolatedNoiseSamplerCopy(random, this.xzScale, this.yScale, this.xzFactor, this.yFactor, this.smearScaleMultiplier);
    }

    public double sample(double blockX, double blockY, double blockZ) {
        double d = blockX * this.scaledXzScale;
        double e = blockY * this.scaledYScale;
        double f = blockZ * this.scaledXzScale;
        double g = d / this.xzFactor;
        double h = e / this.yFactor;
        double i = f / this.xzFactor;
        double j = this.scaledYScale * this.smearScaleMultiplier;
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
}
