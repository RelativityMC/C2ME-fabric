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

import com.google.common.base.Suppliers;
import com.ishland.c2me.opts.dfc.common.ducks.IBlendingAwareVisitor;
import com.ishland.c2me.opts.dfc.common.ducks.IFastCacheLike;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Objects;
import java.util.function.Supplier;

public class CompiledDensityFunction extends SubCompiledDensityFunction {

    private final CompiledEntry compiledEntry;

    public CompiledDensityFunction(CompiledEntry compiledEntry, DensityFunction blendingFallback) {
        super(compiledEntry, compiledEntry, blendingFallback);
        this.compiledEntry = Objects.requireNonNull(compiledEntry);
    }

    private CompiledDensityFunction(CompiledEntry compiledEntry, Supplier<DensityFunction> blendingFallback) {
        super(compiledEntry, compiledEntry, blendingFallback);
        this.compiledEntry = Objects.requireNonNull(compiledEntry);
    }

    @Override
    public DensityFunction applyInternal(DensityFunctionVisitor visitor) {
        if (visitor instanceof IBlendingAwareVisitor blendingAwareVisitor && blendingAwareVisitor.c2me$isBlendingEnabled()) {
            DensityFunction fallback1 = this.getFallback();
            if (fallback1 == null) {
                throw new IllegalStateException("blendingFallback is no more");
            }
            return visitor.apply(fallback1);
        }
        boolean modified = false;
        Object[] args = this.compiledEntry.getArgs();
        for (int i = 0; i < args.length; i ++) {
            Object next = args[i];
            if (next instanceof DensityFunction df) {
                if (!(df instanceof IFastCacheLike)) {
                    DensityFunction applied = visitor.apply(df);
                    if (df != applied) {
                        args[i] = applied;
                        modified = true;
                    }
                }
            }
            if (next instanceof Noise noise) {
                Noise applied = visitor.apply(noise);
                if (noise != applied) {
                    args[i] = applied;
                    modified = true;
                }
            }
        }

        for (int i = 0; i < args.length; i ++) {
            Object next = args[i];
            if (next instanceof IFastCacheLike cacheLike) {
                DensityFunction applied = visitor.apply(cacheLike);
                if (applied == cacheLike.c2me$getDelegate()) {
                    args[i] = null; // cache removed
                    modified = true;
                } else if (applied instanceof IFastCacheLike newCacheLike) {
                    args[i] = newCacheLike;
                    modified = true;
                } else {
                    throw new UnsupportedOperationException("Unsupported transformation on Wrapping node");
                }
            }
        }

        Supplier<DensityFunction> fallback = this.blendingFallback != null ? Suppliers.memoize(() -> {
            DensityFunction densityFunction = this.blendingFallback.get();
            return densityFunction != null ? visitor.apply(densityFunction) : null;
        }) : null;
        if (fallback != this.blendingFallback) {
            modified = true;
        }
        if (modified) {
            return new CompiledDensityFunction(this.compiledEntry.newInstance(args), fallback);
        } else {
            return this;
        }
    }

    @Override
    public DensityFunction apply(DensityFunctionVisitor visitor) {
        if (visitor instanceof IBlendingAwareVisitor blendingAwareVisitor && blendingAwareVisitor.c2me$isBlendingEnabled()) {
            DensityFunction fallback1 = this.getFallback();
            if (fallback1 == null) {
                throw new IllegalStateException("blendingFallback is no more");
            }
            return fallback1.apply(visitor);
        }
        boolean modified = false;
        Object[] args = this.compiledEntry.getArgs();
        for (int i = 0; i < args.length; i ++) {
            Object next = args[i];
            if (next instanceof DensityFunction df) {
                if (!(df instanceof IFastCacheLike)) {
                    DensityFunction applied = df.apply(visitor);
                    if (df != applied) {
                        args[i] = applied;
                        modified = true;
                    }
                }
            }
            if (next instanceof Noise noise) {
                Noise applied = visitor.apply(noise);
                if (noise != applied) {
                    args[i] = applied;
                    modified = true;
                }
            }
        }

        for (int i = 0; i < args.length; i ++) {
            Object next = args[i];
            if (next instanceof IFastCacheLike cacheLike) {
                DensityFunction applied = visitor.apply(cacheLike);
                if (applied == cacheLike.c2me$getDelegate()) {
                    args[i] = null; // cache removed
                    modified = true;
                } else if (applied instanceof IFastCacheLike newCacheLike) {
                    args[i] = newCacheLike;
                    modified = true;
                } else {
                    throw new UnsupportedOperationException("Unsupported transformation on Wrapping node");
                }
            }
        }

        Supplier<DensityFunction> fallback = this.blendingFallback != null ? Suppliers.memoize(() -> {
            DensityFunction densityFunction = this.blendingFallback.get();
            return densityFunction != null ? densityFunction.apply(visitor) : null;
        }) : null;
        if (fallback != this.blendingFallback) {
            modified = true;
        }
        if (modified) {
            return new CompiledDensityFunction(this.compiledEntry.newInstance(args), fallback);
        } else {
            return this;
        }
    }

}
