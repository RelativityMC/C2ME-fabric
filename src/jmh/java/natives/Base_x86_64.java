/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
    private final MethodHandle avx2vnni;
    private final MethodHandle avx512skx;
    private final MethodHandle avx512icl;
    private final MethodHandle avx512spr;

    protected Base_x86_64(MethodHandle base, String prefix) {
        sse2 = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.SSE2.getSuffix()).get());
        sse4_2 = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.SSE4_2.getSuffix()).get());
        avx = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.AVX.getSuffix()).get());
        avx2 = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.AVX2.getSuffix()).get());
        avx2vnni = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.AVX2VNNI.getSuffix()).get());
        avx512skx = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.SKX_AVX512.getSuffix()).get());
        avx512icl = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.ICL_AVX512.getSuffix()).get());
        avx512spr = base.bindTo(NativeLoader.lookup.find(prefix + ISA_x86_64.SPR_AVX512.getSuffix()).get());
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
    public void avx2vnni(Blackhole bh) {
        doInvocation(avx2vnni, bh);
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
