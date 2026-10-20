/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
