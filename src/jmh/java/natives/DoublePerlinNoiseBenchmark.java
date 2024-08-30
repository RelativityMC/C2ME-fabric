package natives;

import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import com.ishland.c2me.opts.natives_math.common.util.MemoryUtil;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import natives.support.ReflectUtils;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.AverageTime})
@OperationsPerInvocation(DoublePerlinNoiseBenchmark.invocations)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class DoublePerlinNoiseBenchmark extends Base_x86_64 {

    protected static final int seed = 0xcafe;
    protected static final int invocations = 1 << 16;

    public static MemorySegment create(DoublePerlinNoiseSampler o) {
        double amplitude = (double) ReflectUtils.getField(DoublePerlinNoiseSampler.class, o, "amplitude");
        OctavePerlinNoiseSampler firstSampler = (OctavePerlinNoiseSampler) ReflectUtils.getField(DoublePerlinNoiseSampler.class, o, "firstSampler");
        OctavePerlinNoiseSampler secondSampler = (OctavePerlinNoiseSampler) ReflectUtils.getField(DoublePerlinNoiseSampler.class, o, "secondSampler");
        long nonNullSamplerCount = 0;
        for (PerlinNoiseSampler sampler : (PerlinNoiseSampler[]) ReflectUtils.getField(OctavePerlinNoiseSampler.class, firstSampler, "octaveSamplers")) {
            if (sampler != null) {
                nonNullSamplerCount++;
            }
        }
        for (PerlinNoiseSampler sampler : (PerlinNoiseSampler[]) ReflectUtils.getField(OctavePerlinNoiseSampler.class, secondSampler, "octaveSamplers")) {
            if (sampler != null) {
                nonNullSamplerCount++;
            }
        }
        final Arena arena = Arena.ofShared(); // this is fine
        final MemorySegment data = arena.allocate(BindingsTemplate.double_octave_sampler_data.byteSize(), 64);
        final MemorySegment need_shift = arena.allocate(nonNullSamplerCount, 64);
        final MemorySegment lacunarity_powd = arena.allocate(nonNullSamplerCount * 8, 64);
        final MemorySegment persistence_powd = arena.allocate(nonNullSamplerCount * 8, 64);
        final MemorySegment sampler_permutations = arena.allocate(nonNullSamplerCount * 256 * 4, 64);
        final MemorySegment sampler_originX = arena.allocate(nonNullSamplerCount * 8, 64);
        final MemorySegment sampler_originY = arena.allocate(nonNullSamplerCount * 8, 64);
        final MemorySegment sampler_originZ = arena.allocate(nonNullSamplerCount * 8, 64);
        final MemorySegment amplitudes = arena.allocate(nonNullSamplerCount * 8, 64);
        BindingsTemplate.double_octave_sampler_data$length.set(data, 0L, nonNullSamplerCount);
        BindingsTemplate.double_octave_sampler_data$amplitude.set(data, 0L, amplitude);
        BindingsTemplate.double_octave_sampler_data$need_shift.set(data, 0L, need_shift);
        BindingsTemplate.double_octave_sampler_data$lacunarity_powd.set(data, 0L, lacunarity_powd);
        BindingsTemplate.double_octave_sampler_data$persistence_powd.set(data, 0L, persistence_powd);
        BindingsTemplate.double_octave_sampler_data$sampler_permutations.set(data, 0L, sampler_permutations);
        BindingsTemplate.double_octave_sampler_data$sampler_originX.set(data, 0L, sampler_originX);
        BindingsTemplate.double_octave_sampler_data$sampler_originY.set(data, 0L, sampler_originY);
        BindingsTemplate.double_octave_sampler_data$sampler_originZ.set(data, 0L, sampler_originZ);
        BindingsTemplate.double_octave_sampler_data$amplitudes.set(data, 0L, amplitudes);
        long index = 0;
        {
            PerlinNoiseSampler[] octaveSamplers = (PerlinNoiseSampler[]) ReflectUtils.getField(OctavePerlinNoiseSampler.class, firstSampler, "octaveSamplers");
            for (int i = 0, octaveSamplersLength = octaveSamplers.length; i < octaveSamplersLength; i++) {
                PerlinNoiseSampler sampler = octaveSamplers[i];
                if (sampler != null) {
                    need_shift.set(ValueLayout.JAVA_BOOLEAN, index, false);
                    lacunarity_powd.set(ValueLayout.JAVA_DOUBLE, index * 8, (double) ReflectUtils.getField(OctavePerlinNoiseSampler.class, firstSampler, "lacunarity") * Math.pow(2.0, i));
                    persistence_powd.set(ValueLayout.JAVA_DOUBLE, index * 8, (double) ReflectUtils.getField(OctavePerlinNoiseSampler.class, firstSampler, "persistence") * Math.pow(2.0, -i));
                    MemorySegment.copy(MemorySegment.ofArray(MemoryUtil.byte2int((byte[]) ReflectUtils.getField(PerlinNoiseSampler.class, sampler, "permutation"))), 0, sampler_permutations, index * 256L * 4L, 256 * 4);
                    sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originX);
                    sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originY);
                    sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originZ);
                    amplitudes.set(ValueLayout.JAVA_DOUBLE, index * 8, ((DoubleList) ReflectUtils.getField(OctavePerlinNoiseSampler.class, firstSampler, "amplitudes")).getDouble(i));
                    index++;
                }
            }
        }
        {
            PerlinNoiseSampler[] octaveSamplers = (PerlinNoiseSampler[]) ReflectUtils.getField(OctavePerlinNoiseSampler.class, secondSampler, "octaveSamplers");
            for (int i = 0, octaveSamplersLength = octaveSamplers.length; i < octaveSamplersLength; i++) {
                PerlinNoiseSampler sampler = octaveSamplers[i];
                if (sampler != null) {
                    need_shift.set(ValueLayout.JAVA_BOOLEAN, index, true);
                    lacunarity_powd.set(ValueLayout.JAVA_DOUBLE, index * 8, (double) ReflectUtils.getField(OctavePerlinNoiseSampler.class, secondSampler, "lacunarity") * Math.pow(2.0, i));
                    persistence_powd.set(ValueLayout.JAVA_DOUBLE, index * 8, (double) ReflectUtils.getField(OctavePerlinNoiseSampler.class, secondSampler, "persistence") * Math.pow(2.0, -i));
                    MemorySegment.copy(MemorySegment.ofArray(MemoryUtil.byte2int((byte[]) ReflectUtils.getField(PerlinNoiseSampler.class, sampler, "permutation"))), 0, sampler_permutations, index * 256L * 4L, 256 * 4);
                    sampler_originX.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originX);
                    sampler_originY.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originY);
                    sampler_originZ.set(ValueLayout.JAVA_DOUBLE, index * 8, sampler.originZ);
                    amplitudes.set(ValueLayout.JAVA_DOUBLE, index * 8, ((DoubleList) ReflectUtils.getField(OctavePerlinNoiseSampler.class, secondSampler, "amplitudes")).getDouble(i));
                    index++;
                }
            }
        }
        return data;
    }

    private final double[] sampleX = new double[invocations];
    private final double[] sampleY = new double[invocations];
    private final double[] sampleZ = new double[invocations];
    private DoublePerlinNoiseSampler vanillaSampler;
    private MemorySegment nativeSamplerData;
    private long nativeSamplerDataPtr;

    @Param({"1", "2", "3", "4", "6", "8", "12", "16", "32", "64"})
    private int scale;

    public DoublePerlinNoiseBenchmark() {
        super(BindingsTemplate.c2me_natives_noise_perlin_double_octave_sample_ptr, "c2me_natives_noise_perlin_double_octave_sample");
    }

    @Setup(Level.Trial)
    public void setup() {
        Random random = new Random(seed);
        for (int i = 0; i < invocations; i++) {
            sampleX[i] = random.nextDouble(-30000000.0D, 30000000.0D);
            sampleY[i] = random.nextDouble(-2048.0D, 2048.0D);
            sampleZ[i] = random.nextDouble(-30000000.0D, 30000000.0D);
        }
        final net.minecraft.util.math.random.Random minecraftRandom = net.minecraft.util.math.random.Random.create(random.nextInt());
        double[] octaves = new double[scale];
        for (int i = 0, octavesLength = octaves.length; i < octavesLength; i++) {
            octaves[i] = minecraftRandom.nextDouble() * 32.0D + 0.01D;
        }
        vanillaSampler = DoublePerlinNoiseSampler.create(minecraftRandom, minecraftRandom.nextInt(128), octaves);
        nativeSamplerData = create(vanillaSampler);
        nativeSamplerDataPtr = nativeSamplerData.address();
    }

    @Override
    protected void doInvocation(MethodHandle handle, Blackhole bh) {
        for (int i = 0; i < invocations; i ++) {
            try {
                bh.consume((double) handle.invokeExact(nativeSamplerDataPtr, sampleX[i], sampleY[i], sampleZ[i]));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Benchmark
    @Override
    public void spinning(Blackhole bh) {
        for (int i = 0; i < invocations; i ++) {
            bh.consume(sampleX[i] + sampleY[i] + sampleZ[i]);
        }
    }

    @Benchmark
    @Override
    public void vanilla(Blackhole bh) {
        for (int i = 0; i < invocations; i ++) {
            bh.consume(vanillaSampler.sample(sampleX[i], sampleY[i], sampleZ[i]));
        }
    }
}
