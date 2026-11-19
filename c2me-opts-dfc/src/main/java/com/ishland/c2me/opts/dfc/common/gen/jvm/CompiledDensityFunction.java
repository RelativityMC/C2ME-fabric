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

package com.ishland.c2me.opts.dfc.common.gen.jvm;

import com.google.common.base.Suppliers;
import com.ishland.c2me.opts.dfc.common.ducks.IBlendingAwareVisitor;
import com.ishland.c2me.opts.dfc.common.ducks.ICompiledCachingAwareVisitor;
import com.ishland.c2me.opts.dfc.common.gen.jvm.internalapi.IMultiMethod;
import com.ishland.c2me.opts.dfc.common.gen.jvm.internalapi.ISingleMethod;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Objects;
import java.util.function.Supplier;

public class CompiledDensityFunction extends AbstractCompiledDensityFunction {

    private final int compiledIndex;
    private CompiledEntry compiledEntry = null;
    private ISingleMethod singleMethod = null;
    private IMultiMethod multiMethod = null;

    CompiledDensityFunction(int compiledIndex, DensityFunction blendingFallback) {
        this(compiledIndex, unwrapFallback(blendingFallback));
    }

    private CompiledDensityFunction(int compiledIndex, Supplier<DensityFunction> blendingFallback) {
        super(blendingFallback);
        this.compiledIndex = compiledIndex;
    }

    @Override
    public ISingleMethod getSingleMethod() {
        return this.singleMethod;
    }

    @Override
    public IMultiMethod getMultiMethod() {
        return this.multiMethod;
    }

    public synchronized void initFrom(CompiledEntry entry) {
        if (this.compiledEntry != null) {
            throw new IllegalStateException("Already initialized");
        }

        Objects.requireNonNull(entry);
        SubCompiledDensityFunction method = entry.getRootsUnsafe()[this.compiledIndex];
        this.compiledEntry = entry;
        this.singleMethod = Objects.requireNonNull(method.singleMethod);
        this.multiMethod = Objects.requireNonNull(method.multiMethod);
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
        CompiledEntry compiledEntry = this.compiledEntry;
        ISingleMethod singleMethod = this.singleMethod;
        IMultiMethod multiMethod = this.multiMethod;
        if (compiledEntry == null || singleMethod == null || multiMethod == null) {
            throw new IllegalStateException("Attempted to apply a incomplete compiled df");
        }

        Supplier<DensityFunction> fallback = this.blendingFallback != null ? Suppliers.memoize(() -> {
            DensityFunction densityFunction = this.blendingFallback.get();
            return densityFunction != null ? visitor.apply(densityFunction) : null;
        }) : null;
        CompiledDensityFunction function = new CompiledDensityFunction(this.compiledIndex, fallback);
        CompiledEntry initializedEntry;
        if (visitor instanceof ICompiledCachingAwareVisitor compiledCachingAwareVisitor) {
            initializedEntry = compiledCachingAwareVisitor.c2me$visitIfAbsent(compiledEntry, ICompiledCachingAwareVisitor.c2me$getArgumentVisitor(visitor));
        } else {
            initializedEntry = this.compiledEntry.newInstance(compiledEntry.getArgs(), ICompiledCachingAwareVisitor.c2me$getArgumentVisitor(visitor));
        }
        function.initFrom(initializedEntry);
        return function;
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
        CompiledEntry compiledEntry = this.compiledEntry;
        ISingleMethod singleMethod = this.singleMethod;
        IMultiMethod multiMethod = this.multiMethod;
        if (compiledEntry == null || singleMethod == null || multiMethod == null) {
            throw new IllegalStateException("Attempted to apply a incomplete compiled df");
        }
        Supplier<DensityFunction> fallback = this.blendingFallback != null ? Suppliers.memoize(() -> {
            DensityFunction densityFunction = this.blendingFallback.get();
            return densityFunction != null ? visitor.apply(densityFunction) : null;
        }) : null;
        CompiledDensityFunction function = new CompiledDensityFunction(this.compiledIndex, fallback);
        CompiledEntry initializedEntry;
        if (visitor instanceof ICompiledCachingAwareVisitor compiledCachingAwareVisitor) {
            initializedEntry = compiledCachingAwareVisitor.c2me$visitIfAbsent(compiledEntry, ICompiledCachingAwareVisitor.c2me$getArgumentVisitor(visitor));
        } else {
            initializedEntry = this.compiledEntry.newInstance(compiledEntry.getArgs(), ICompiledCachingAwareVisitor.c2me$getArgumentVisitor(visitor));
        }
        function.initFrom(initializedEntry);
        return function;
    }

}
