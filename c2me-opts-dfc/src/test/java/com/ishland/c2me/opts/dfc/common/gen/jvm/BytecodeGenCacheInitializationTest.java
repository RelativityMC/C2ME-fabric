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

package com.ishland.c2me.opts.dfc.common.gen.jvm;

import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.ast.opto.OptoPasses;
import com.ishland.c2me.opts.dfc.common.ducks.IFastCacheLike;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BytecodeGenCacheInitializationTest {

    private static final DensityFunction.NoisePos ORIGIN = new TestNoisePos(0, 0, 0);

    @BeforeAll
    public static void bootstrap() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    public void materializesCacheFieldsChildFirstDuringConstruction() {
        TestCache innerCache = new TestCache("inner", null, false);
        TestCache outerCache = new TestCache("outer", null, false);
        CacheLikeNode inner = new CacheLikeNode(innerCache, new ConstantNode(1.0));
        CacheLikeNode outer = new CacheLikeNode(outerCache, inner);

        CompiledEntry entry = BytecodeGen.compile0("cache_init_order", OptoPasses.AstPair.ofOptimizedOnly(outer));
        List<String> materializationOrder = new ArrayList<>();
        CompiledEntry transformed = entry.newInstance(entry.getArgs(), cacheLike -> {
            TestCache cache = (TestCache) cacheLike;
            materializationOrder.add(cache.name);
            if (cache.name.equals("outer")) {
                assertEquals(42.0, cache.delegate.sample(ORIGIN));
            }
            return cache.materialized();
        });

        assertEquals(List.of("inner", "outer"), materializationOrder);
        assertTrue(((TestCache) transformed.getArgs()[0]).materialized);
        for (java.lang.reflect.Field field : entry.getClass().getDeclaredFields()) {
            assertTrue(Modifier.isFinal(field.getModifiers()), field::toString);
        }
    }

    @Test
    public void skipsTransformerForRemovedCache() {
        TestCache innerCache = new TestCache("inner", null, false);
        TestCache outerCache = new TestCache("outer", null, false);
        CacheLikeNode inner = new CacheLikeNode(innerCache, new ConstantNode(1.0));
        CacheLikeNode outer = new CacheLikeNode(outerCache, inner);
        CompiledEntry entry = BytecodeGen.compile0("removed_cache", OptoPasses.AstPair.ofOptimizedOnly(outer));
        Object[] args = entry.getArgs();
        args[0] = null;
        AtomicInteger transformations = new AtomicInteger();

        CompiledEntry transformed = entry.newInstance(args, cacheLike -> {
            transformations.incrementAndGet();
            return cacheLike;
        });

        assertNull(transformed.getArgs()[0]);
        assertEquals(1, transformations.get());
    }

    private static final class TestCache implements IFastCacheLike {

        private final String name;
        private final DensityFunction delegate;
        private final boolean materialized;

        private TestCache(String name, DensityFunction delegate, boolean materialized) {
            this.name = name;
            this.delegate = delegate;
            this.materialized = materialized;
        }

        private TestCache materialized() {
            return new TestCache(this.name, this.delegate, true);
        }

        @Override
        public double sample(NoisePos pos) {
            return this.delegate.sample(pos);
        }

        @Override
        public void fill(double[] densities, EachApplier applier) {
            this.delegate.fill(densities, applier);
        }

        @Override
        public DensityFunction applyInternal(DensityFunctionVisitor visitor) {
            return visitor.apply(this);
        }

        @Override
        public double minValue() {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double maxValue() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double c2me$getCached(int x, int y, int z, EvalType evalType) {
            return this.materialized ? 42.0 : Double.longBitsToDouble(CACHE_MISS_NAN_BITS);
        }

        @Override
        public boolean c2me$getCached(double[] res, int[] x, int[] y, int[] z, EvalType evalType) {
            return false;
        }

        @Override
        public void c2me$cache(int x, int y, int z, EvalType evalType, double cached) {
        }

        @Override
        public void c2me$cache(double[] res, int[] x, int[] y, int[] z, EvalType evalType) {
        }

        @Override
        public boolean c2me$isActualCache() {
            return this.materialized;
        }

        @Override
        public String c2me$describeCacheLike() {
            return this.name;
        }

        @Override
        public DensityFunction c2me$getDelegate() {
            return this.delegate;
        }

        @Override
        public DensityFunction c2me$withDelegate(DensityFunction delegate) {
            return new TestCache(this.name, delegate, this.materialized);
        }
    }

    private record TestNoisePos(int blockX, int blockY, int blockZ) implements DensityFunction.NoisePos {
    }
}
