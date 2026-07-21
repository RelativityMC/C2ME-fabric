package natives;

import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import natives.support.FastNoiseLiteCopy;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.AverageTime})
@OperationsPerInvocation(PerlinNoiseBenchmark.invocations)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class FastNoiseLiteBenchmark extends Base_x86_64 {

    protected static final int seed = 0xcafe;
    protected static final int invocations = 1 << 16;

    public static MemorySegment create(FastNoiseLiteCopy fnl) {
        final Arena arena = Arena.ofShared(); // this is fine
        final MemorySegment data = arena.allocate(BindingsTemplate.fnl_state.byteSize());
        BindingsTemplate.fnl_state$seed.set(data, 0L, fnl.mSeed);
        BindingsTemplate.fnl_state$frequency.set(data, 0L, fnl.mFrequency);
        BindingsTemplate.fnl_state$noise_type.set(data, 0L, fnl.mNoiseType.ordinal());
        BindingsTemplate.fnl_state$rotation_type_3d.set(data, 0L, fnl.mRotationType3D.ordinal());
        BindingsTemplate.fnl_state$fractal_type.set(data, 0L, fnl.mFractalType.ordinal());
        BindingsTemplate.fnl_state$octaves.set(data, 0L, fnl.mOctaves);
        BindingsTemplate.fnl_state$lacunarity.set(data, 0L, fnl.mLacunarity);
        BindingsTemplate.fnl_state$gain.set(data, 0L, fnl.mGain);
        BindingsTemplate.fnl_state$weighted_strength.set(data, 0L, fnl.mWeightedStrength);
        BindingsTemplate.fnl_state$ping_pong_strength.set(data, 0L, fnl.mPingPongStrength);
        BindingsTemplate.fnl_state$cellular_distance_func.set(data, 0L, fnl.mCellularDistanceFunction.ordinal());
        BindingsTemplate.fnl_state$cellular_return_type.set(data, 0L, fnl.mCellularReturnType.ordinal());
        BindingsTemplate.fnl_state$cellular_jitter_mod.set(data, 0L, fnl.mCellularJitterModifier);
        BindingsTemplate.fnl_state$domain_warp_type.set(data, 0L, fnl.mDomainWarpType.ordinal());
        BindingsTemplate.fnl_state$domain_warp_amp.set(data, 0L, fnl.mDomainWarpAmp);
        return data;
    }

    @Param
    private FastNoiseLiteCopy.NoiseType noiseType;

    public FastNoiseLiteBenchmark() {
        super(BindingsTemplate.c2me_natives_fnlGetNoise3D_ptr, "c2me_natives_fnlGetNoise3D");
    }

    private final double[] sampleX = new double[invocations];
    private final double[] sampleY = new double[invocations];
    private final double[] sampleZ = new double[invocations];
    private FastNoiseLiteCopy fnl;
    private MemorySegment fnlState;
    private long fnlStatePtr;

    @Setup(Level.Trial)
    public void setup() {
        Random random = new Random(seed);
        for (int i = 0; i < invocations; i++) {
            sampleX[i] = random.nextDouble(-30000000.0D, 30000000.0D);
            sampleY[i] = random.nextDouble(-2048.0D, 2048.0D);
            sampleZ[i] = random.nextDouble(-30000000.0D, 30000000.0D);
        }
        fnl = new FastNoiseLiteCopy(seed);
        fnl.SetNoiseType(noiseType);
        fnlState = create(fnl);
        fnlStatePtr = fnlState.address();
    }

    @Override
    protected void doInvocation(MethodHandle handle, Blackhole bh) {
        for (int i = 0; i < invocations; i ++) {
            try {
                bh.consume((float) handle.invokeExact(fnlStatePtr, sampleX[i], sampleY[i], sampleZ[i]));
            } catch (Throwable t) {
                throw new RuntimeException(t);
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

    @Override
    @Benchmark
    public void vanilla(Blackhole bh) {
        for (int i = 0; i < invocations; i ++) {
            bh.consume(fnl.GetNoise((float) sampleX[i], (float) sampleY[i], (float) sampleZ[i]));
        }
    }
}
