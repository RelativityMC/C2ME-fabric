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

package nonnative;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OperationsPerInvocation(ThreadLocalBenchmark.invocations)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ThreadLocalBenchmark {

    public static final int invocations = 32768;

    private ThreadLocal<Object>[] noise;
    private ThreadLocal<Object> theThing;

    @Setup(Level.Trial)
    public void setup() {
        this.noise = IntStream.range(0, 16)
                .mapToObj(value -> {
                    ThreadLocal<Object> threadLocal = new ThreadLocal<>();
                    threadLocal.set(new Object());
                    return threadLocal;
                })
                .toArray(ThreadLocal[]::new);
        this.theThing = ThreadLocal.withInitial(() -> new Object());
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    private static <T> T getThreadLocal(ThreadLocal<T> threadLocal) {
        return threadLocal.get();
    }

    @Benchmark
    public void bench(Blackhole bh) {
        for (int i = 0; i < invocations; i ++) {
            bh.consume(getThreadLocal(this.theThing));
        }
    }

}
