package natives;

import com.ishland.c2me.opts.natives_math.common.NativeLoader;
import com.ishland.c2me.opts.natives_math.common.isa.ISA_x86_64;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.invoke.MethodHandle;

public abstract class Base_x86_64 {

    private final MethodHandle sse2;
    private final MethodHandle sse4_2;
    private final MethodHandle avx;
    private final MethodHandle avx2;
    private final MethodHandle avx2adl;
    private final MethodHandle avx512skx;
    private final MethodHandle avx512icl;
    private final MethodHandle avx512spr;

    protected Base_x86_64(MethodHandle base, String prefix) {
        sse2 = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.SSE2.getSuffix()).get());
        sse4_2 = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.SSE4_2.getSuffix()).get());
        avx = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.AVX.getSuffix()).get());
        avx2 = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.AVX2.getSuffix()).get());
        avx2adl = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.AVX2ADL.getSuffix()).get());
        avx512skx = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.AVX512SKX.getSuffix()).get());
        avx512icl = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.AVX512ICL.getSuffix()).get());
        avx512spr = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.AVX512SPR.getSuffix()).get());
    }

    @CompilerControl(CompilerControl.Mode.INLINE)
    protected abstract void doInvocation(MethodHandle handle, Blackhole bh);

    public abstract void spinning(Blackhole bh);

    public abstract void vanilla(Blackhole bh);

    @Benchmark
    public void sse2(Blackhole bh) {
        doInvocation(sse2, bh);
    }

    @Benchmark
    public void sse4_2(Blackhole bh) {
        doInvocation(sse4_2, bh);
    }

    @Benchmark
    public void avx(Blackhole bh) {
        doInvocation(avx, bh);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 10)
    public void avx2(Blackhole bh) {
        doInvocation(avx2, bh);
    }

    @Benchmark
    public void avx2adl(Blackhole bh) {
        doInvocation(avx2adl, bh);
    }

    @Benchmark
    public void avx512skx(Blackhole bh) {
        doInvocation(avx512skx, bh);
    }

    @Benchmark
    public void avx512icl(Blackhole bh) {
        doInvocation(avx512icl, bh);
    }

    @Benchmark
    public void avx512spr(Blackhole bh) {
        doInvocation(avx512spr, bh);
    }

}
