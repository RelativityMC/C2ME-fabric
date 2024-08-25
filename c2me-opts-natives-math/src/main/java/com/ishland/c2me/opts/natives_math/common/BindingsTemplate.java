package com.ishland.c2me.opts.natives_math.common;

import com.ishland.c2me.base.mixin.access.IInterpolatedNoiseSampler;
import com.ishland.c2me.base.mixin.access.IOctavePerlinNoiseSampler;
import com.ishland.c2me.base.mixin.access.IPerlinNoiseSampler;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BindingsTemplate {

    // double c2me_natives_noise_perlin_sample (const uint8_t *permutations, double originX, double originY, double originZ, double x, double y, double z, double yScale, double yMax)
    public static final MethodHandle c2me_natives_noise_perlin_sample = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE
            ),
            Linker.Option.critical(true)
    );

    // c2me_natives_noise_perlin_double_octave_sample, double, (const double_octave_sampler_data_t *data, double x, double y, double z)
    public static final MethodHandle c2me_natives_noise_perlin_double_octave_sample = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE
            ),
            Linker.Option.critical(false)
    );
    public static final MethodHandle c2me_natives_noise_perlin_double_octave_sample_ptr = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE
            ),
            Linker.Option.critical(false)
    );

    public static final StructLayout double_octave_sampler_data = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("length"),
            ValueLayout.ADDRESS.withName("need_shift"),
            ValueLayout.ADDRESS.withName("lacunarity_powd"),
            ValueLayout.ADDRESS.withName("persistence_powd"),
            ValueLayout.ADDRESS.withName("sampler_permutations"),
            ValueLayout.ADDRESS.withName("sampler_originX"),
            ValueLayout.ADDRESS.withName("sampler_originY"),
            ValueLayout.ADDRESS.withName("sampler_originZ"),
            ValueLayout.ADDRESS.withName("amplitudes")
    ).withByteAlignment(32).withName("double_double_octave_sampler_data");
    public static final VarHandle double_octave_sampler_data$length = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("length"));
    public static final VarHandle double_octave_sampler_data$need_shift = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("need_shift"));
    public static final VarHandle double_octave_sampler_data$lacunarity_powd = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("lacunarity_powd"));
    public static final VarHandle double_octave_sampler_data$persistence_powd = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("persistence_powd"));
    public static final VarHandle double_octave_sampler_data$sampler_permutations = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("sampler_permutations"));
    public static final VarHandle double_octave_sampler_data$sampler_originX = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("sampler_originX"));
    public static final VarHandle double_octave_sampler_data$sampler_originY = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("sampler_originY"));
    public static final VarHandle double_octave_sampler_data$sampler_originZ = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("sampler_originZ"));
    public static final VarHandle double_octave_sampler_data$amplitudes = double_octave_sampler_data.varHandle(MemoryLayout.PathElement.groupElement("amplitudes"));

    public static MemorySegment double_octave_sampler_data$create(Arena arena, OctavePerlinNoiseSampler firstSampler, OctavePerlinNoiseSampler secondSampler) {
        long nonNullSamplerCount = 0;
        for (PerlinNoiseSampler sampler : ((IOctavePerlinNoiseSampler) firstSampler).getOctaveSamplers()) {
            if (sampler != null) {
                nonNullSamplerCount++;
            }
        }
        for (PerlinNoiseSampler sampler : ((IOctavePerlinNoiseSampler) secondSampler).getOctaveSamplers()) {
            if (sampler != null) {
                nonNullSamplerCount++;
            }
        }
        final MemorySegment data = arena.allocate(double_octave_sampler_data.byteSize(), 64);
        final MemorySegment need_shift = arena.allocate(nonNullSamplerCount, 64);
        final MemorySegment lacunarity_powd = arena.allocate(nonNullSamplerCount * 8, 64);
        final MemorySegment persistence_powd = arena.allocate(nonNullSamplerCount * 8, 64);
        final MemorySegment sampler_permutations = arena.allocate(nonNullSamplerCount * 256, 64);
        final MemorySegment sampler_originX = arena.allocate(nonNullSamplerCount * 8, 64);
        final MemorySegment sampler_originY = arena.allocate(nonNullSamplerCount * 8, 64);
        final MemorySegment sampler_originZ = arena.allocate(nonNullSamplerCount * 8, 64);
        final MemorySegment amplitudes = arena.allocate(nonNullSamplerCount * 8, 64);
        double_octave_sampler_data$length.set(data, 0L, nonNullSamplerCount);
        double_octave_sampler_data$need_shift.set(data, 0L, need_shift);
        double_octave_sampler_data$lacunarity_powd.set(data, 0L, lacunarity_powd);
        double_octave_sampler_data$persistence_powd.set(data, 0L, persistence_powd);
        double_octave_sampler_data$sampler_permutations.set(data, 0L, sampler_permutations);
        double_octave_sampler_data$sampler_originX.set(data, 0L, sampler_originX);
        double_octave_sampler_data$sampler_originY.set(data, 0L, sampler_originY);
        double_octave_sampler_data$sampler_originZ.set(data, 0L, sampler_originZ);
        double_octave_sampler_data$amplitudes.set(data, 0L, amplitudes);
        long index = 0;
        {
            PerlinNoiseSampler[] octaveSamplers = ((IOctavePerlinNoiseSampler) firstSampler).getOctaveSamplers();
            for (int i = 0, octaveSamplersLength = octaveSamplers.length; i < octaveSamplersLength; i++) {
                PerlinNoiseSampler sampler = octaveSamplers[i];
                if (sampler != null) {
                    need_shift.set(ValueLayout.JAVA_BOOLEAN, index, false);
                    lacunarity_powd.set(ValueLayout.JAVA_DOUBLE, index * 8, ((IOctavePerlinNoiseSampler) firstSampler).getLacunarity() * Math.pow(2.0, i));
                    persistence_powd.set(ValueLayout.JAVA_DOUBLE, index * 8, ((IOctavePerlinNoiseSampler) firstSampler).getPersistence() * Math.pow(2.0, -i));
                    MemorySegment.copy(MemorySegment.ofArray(((IPerlinNoiseSampler) (Object) sampler).getPermutation()), 0, sampler_permutations, index * 256L, 256);
                    sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originX);
                    sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originY);
                    sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originZ);
                    amplitudes.set(ValueLayout.JAVA_DOUBLE, index * 8, ((IOctavePerlinNoiseSampler) firstSampler).getAmplitudes().getDouble(i));
                    index++;
                }
            }
        }
        {
            PerlinNoiseSampler[] octaveSamplers = ((IOctavePerlinNoiseSampler) secondSampler).getOctaveSamplers();
            for (int i = 0, octaveSamplersLength = octaveSamplers.length; i < octaveSamplersLength; i++) {
                PerlinNoiseSampler sampler = octaveSamplers[i];
                if (sampler != null) {
                    need_shift.set(ValueLayout.JAVA_BOOLEAN, index, true);
                    lacunarity_powd.set(ValueLayout.JAVA_DOUBLE, index * 8, ((IOctavePerlinNoiseSampler) secondSampler).getLacunarity() * Math.pow(2.0, i));
                    persistence_powd.set(ValueLayout.JAVA_DOUBLE, index * 8, ((IOctavePerlinNoiseSampler) secondSampler).getPersistence() * Math.pow(2.0, -i));
                    MemorySegment.copy(MemorySegment.ofArray(((IPerlinNoiseSampler) (Object) sampler).getPermutation()), 0, sampler_permutations, index * 256L, 256);
                    sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originX);
                    sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originY);
                    sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originZ);
                    amplitudes.set(ValueLayout.JAVA_DOUBLE, index * 8, ((IOctavePerlinNoiseSampler) secondSampler).getAmplitudes().getDouble(i));
                    index++;
                }
            }
        }
        return data;
    }

    // c2me_natives_noise_perlin_interpolated_sample, double, (const interpolated_noise_sampler_t *const data, const double x, const double y, const double z)
    public static final MethodHandle c2me_natives_noise_perlin_interpolated_sample = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE
            ),
            Linker.Option.critical(false)
    );
    public static final MethodHandle c2me_natives_noise_perlin_interpolated_sample_ptr = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE
            ),
            Linker.Option.critical(false)
    );

    // typedef struct interpolated_noise_sampler {
    //     const double scaledXzScale;
    //     const double scaledYScale;
    //     const double xzFactor;
    //     const double yFactor;
    //     const double smearScaleMultiplier;
    //     const double xzScale;
    //     const double yScale;
    //
    //     const uint8 *sampler_permutations;
    //     const double *sampler_originX;
    //     const double *sampler_originY;
    //     const double *sampler_originZ;
    //     const double *sampler_mulFactor;
    //
    //     const int32 upperNoiseOffset;
    //     const int32 normalNoiseOffset;
    //     const int32 endOffset;
    // } interpolated_noise_sampler_t;
    public static final StructLayout interpolated_noise_sampler = MemoryLayout.structLayout(
            ValueLayout.JAVA_DOUBLE.withName("scaledXzScale"),
            ValueLayout.JAVA_DOUBLE.withName("scaledYScale"),
            ValueLayout.JAVA_DOUBLE.withName("xzFactor"),
            ValueLayout.JAVA_DOUBLE.withName("yFactor"),
            ValueLayout.JAVA_DOUBLE.withName("smearScaleMultiplier"),
            ValueLayout.JAVA_DOUBLE.withName("xzScale"),
            ValueLayout.JAVA_DOUBLE.withName("yScale"),

            ValueLayout.ADDRESS.withName("sampler_permutations"),
            ValueLayout.ADDRESS.withName("sampler_originX"),
            ValueLayout.ADDRESS.withName("sampler_originY"),
            ValueLayout.ADDRESS.withName("sampler_originZ"),
            ValueLayout.ADDRESS.withName("sampler_mulFactor"),

            ValueLayout.JAVA_INT.withName("upperNoiseOffset"),
            ValueLayout.JAVA_INT.withName("normalNoiseOffset"),
            ValueLayout.JAVA_INT.withName("endOffset")
    ).withByteAlignment(32).withName("interpolated_noise_sampler_t");

    public static final VarHandle interpolated_noise_sampler$scaledXzScale = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("scaledXzScale"));
    public static final VarHandle interpolated_noise_sampler$scaledYScale = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("scaledYScale"));
    public static final VarHandle interpolated_noise_sampler$xzFactor = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("xzFactor"));
    public static final VarHandle interpolated_noise_sampler$yFactor = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("yFactor"));
    public static final VarHandle interpolated_noise_sampler$smearScaleMultiplier = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("smearScaleMultiplier"));
    public static final VarHandle interpolated_noise_sampler$xzScale = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("xzScale"));
    public static final VarHandle interpolated_noise_sampler$yScale = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("yScale"));
    public static final VarHandle interpolated_noise_sampler$sampler_permutations = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("sampler_permutations"));
    public static final VarHandle interpolated_noise_sampler$sampler_originX = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("sampler_originX"));
    public static final VarHandle interpolated_noise_sampler$sampler_originY = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("sampler_originY"));
    public static final VarHandle interpolated_noise_sampler$sampler_originZ = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("sampler_originZ"));
    public static final VarHandle interpolated_noise_sampler$sampler_mulFactor = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("sampler_mulFactor"));
    public static final VarHandle interpolated_noise_sampler$upperNoiseOffset = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("upperNoiseOffset"));
    public static final VarHandle interpolated_noise_sampler$normalNoiseOffset = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("normalNoiseOffset"));
    public static final VarHandle interpolated_noise_sampler$endOffset = interpolated_noise_sampler.varHandle(MemoryLayout.PathElement.groupElement("endOffset"));

    public static boolean interpolated_noise_sampler$isSpecializedBase3dNoiseFunction(InterpolatedNoiseSampler interpolated) {
        return IntStream.range(0, 16).mapToObj(((IInterpolatedNoiseSampler) interpolated).getLowerInterpolatedNoise()::getOctave).filter(Objects::nonNull).count() == 16 &&
                IntStream.range(0, 16).mapToObj(((IInterpolatedNoiseSampler) interpolated).getUpperInterpolatedNoise()::getOctave).filter(Objects::nonNull).count() == 16 &&
                IntStream.range(0, 8).mapToObj(((IInterpolatedNoiseSampler) interpolated).getInterpolationNoise()::getOctave).filter(Objects::nonNull).count() == 8;
    }

    public static MemorySegment interpolated_noise_sampler$create(Arena arena, InterpolatedNoiseSampler interpolated) {
        final MemorySegment data = arena.allocate(interpolated_noise_sampler.byteSize(), 64);
        interpolated_noise_sampler$scaledXzScale.set(data, 0L, ((IInterpolatedNoiseSampler) interpolated).getScaledXzScale());
        interpolated_noise_sampler$scaledYScale.set(data, 0L, ((IInterpolatedNoiseSampler) interpolated).getScaledYScale());
        interpolated_noise_sampler$xzFactor.set(data, 0L, ((IInterpolatedNoiseSampler) interpolated).getXzFactor());
        interpolated_noise_sampler$yFactor.set(data, 0L, ((IInterpolatedNoiseSampler) interpolated).getYFactor());
        interpolated_noise_sampler$smearScaleMultiplier.set(data, 0L, ((IInterpolatedNoiseSampler) interpolated).getSmearScaleMultiplier());
        interpolated_noise_sampler$xzScale.set(data, 0L, ((IInterpolatedNoiseSampler) interpolated).getXzScale());
        interpolated_noise_sampler$yScale.set(data, 0L, ((IInterpolatedNoiseSampler) interpolated).getYScale());
        int countNonNull = Math.toIntExact(Stream.of(
                IntStream.range(0, 8).mapToObj(((IInterpolatedNoiseSampler) interpolated).getInterpolationNoise()::getOctave),
                IntStream.range(0, 16).mapToObj(((IInterpolatedNoiseSampler) interpolated).getLowerInterpolatedNoise()::getOctave),
                IntStream.range(0, 16).mapToObj(((IInterpolatedNoiseSampler) interpolated).getUpperInterpolatedNoise()::getOctave)
        ).flatMap(Function.identity()).filter(Objects::nonNull).count());

//        if (true) {
//            System.out.println(String.format("Interpolated total: %d", countNonNull));
//            System.out.println(String.format("lower: %d", IntStream.range(0, 16).mapToObj(((IInterpolatedNoiseSampler) interpolated).getLowerInterpolatedNoise()::getOctave).filter(Objects::nonNull).count()));
//            System.out.println(String.format("upper: %d", IntStream.range(0, 16).mapToObj(((IInterpolatedNoiseSampler) interpolated).getUpperInterpolatedNoise()::getOctave).filter(Objects::nonNull).count()));
//            System.out.println(String.format("normal: %d", IntStream.range(0, 8).mapToObj(((IInterpolatedNoiseSampler) interpolated).getInterpolationNoise()::getOctave).filter(Objects::nonNull).count()));
//        }

        final MemorySegment sampler_permutations = arena.allocate(countNonNull * 256L, 64);
        final MemorySegment sampler_originX = arena.allocate(countNonNull * 8L, 64);
        final MemorySegment sampler_originY = arena.allocate(countNonNull * 8L, 64);
        final MemorySegment sampler_originZ = arena.allocate(countNonNull * 8L, 64);
        final MemorySegment sampler_mulFactor = arena.allocate(countNonNull * 8L, 64);

        int index = 0;
        for (int i = 0; i < 16; i++) {
            PerlinNoiseSampler sampler = ((IInterpolatedNoiseSampler) interpolated).getLowerInterpolatedNoise().getOctave(i);
            if (sampler != null) {
                MemorySegment.copy(MemorySegment.ofArray(((IPerlinNoiseSampler) (Object) sampler).getPermutation()), 0, sampler_permutations, index * 256L, 256);
                sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originX);
                sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originY);
                sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originZ);
                sampler_mulFactor.set(ValueLayout.JAVA_DOUBLE, index * 8L, Math.pow(2, -i));
                index++;
            }
        }

        interpolated_noise_sampler$upperNoiseOffset.set(data, 0L, index);

        for (int i = 0; i < 16; i++) {
            PerlinNoiseSampler sampler = ((IInterpolatedNoiseSampler) interpolated).getUpperInterpolatedNoise().getOctave(i);
            if (sampler != null) {
                MemorySegment.copy(MemorySegment.ofArray(((IPerlinNoiseSampler) (Object) sampler).getPermutation()), 0, sampler_permutations, index * 256L, 256);
                sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originX);
                sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originY);
                sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originZ);
                sampler_mulFactor.set(ValueLayout.JAVA_DOUBLE, index * 8L, Math.pow(2, -i));
                index++;
            }
        }

        interpolated_noise_sampler$normalNoiseOffset.set(data, 0L, index);

        for (int i = 0; i < 8; i++) {
            PerlinNoiseSampler sampler = ((IInterpolatedNoiseSampler) interpolated).getInterpolationNoise().getOctave(i);
            if (sampler != null) {
                MemorySegment.copy(MemorySegment.ofArray(((IPerlinNoiseSampler) (Object) sampler).getPermutation()), 0, sampler_permutations, index * 256L, 256);
                sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originX);
                sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originY);
                sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8L, sampler.originZ);
                sampler_mulFactor.set(ValueLayout.JAVA_DOUBLE, index * 8L, Math.pow(2, -i));
                index++;
            }
        }

        interpolated_noise_sampler$endOffset.set(data, 0L, index);

        interpolated_noise_sampler$sampler_permutations.set(data, 0L, sampler_permutations);
        interpolated_noise_sampler$sampler_originX.set(data, 0L, sampler_originX);
        interpolated_noise_sampler$sampler_originY.set(data, 0L, sampler_originY);
        interpolated_noise_sampler$sampler_originZ.set(data, 0L, sampler_originZ);
        interpolated_noise_sampler$sampler_mulFactor.set(data, 0L, sampler_mulFactor);

        if (index != countNonNull) {
            throw new AssertionError("index != countNonNull");
        }

        VarHandle.fullFence();

        return data;
    }

}
