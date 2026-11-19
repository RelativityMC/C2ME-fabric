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

package com.ishland.c2me.opts.dfc.common.gen;

import com.ishland.c2me.opts.dfc.common.ducks.IBlendingAwareVisitor;
import com.ishland.c2me.opts.dfc.common.ducks.ICompiledCachingAwareVisitor;
import com.ishland.c2me.opts.dfc.common.gen.jvm.CompiledEntry;
import com.ishland.c2me.opts.dfc.common.gen.jvm.internalapi.ArgumentVisitor;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Map;
import java.util.Objects;

public class DelegatingBlendingCachingAwareVisitor implements IBlendingAwareVisitor, ICompiledCachingAwareVisitor, DensityFunction.DensityFunctionVisitor {

    private final DensityFunction.DensityFunctionVisitor delegate;
    private final Map<CompiledEntry, CompiledEntry> backingCache;
    private final boolean blendingEnabled;

    public DelegatingBlendingCachingAwareVisitor(DensityFunction.DensityFunctionVisitor delegate, Map<CompiledEntry, CompiledEntry> backingCache, boolean blendingEnabled) {
        this.delegate = Objects.requireNonNull(delegate);
        this.backingCache = Objects.requireNonNull(backingCache);
        this.blendingEnabled = blendingEnabled;
    }

    @Override
    public DensityFunction apply(DensityFunction densityFunction) {
        return this.delegate.apply(densityFunction);
    }

    @Override
    public DensityFunction.Noise apply(DensityFunction.Noise noiseDensityFunction) {
        return this.delegate.apply(noiseDensityFunction);
    }

    @Override
    public boolean c2me$isBlendingEnabled() {
        return this.blendingEnabled;
    }

    @Override
    public CompiledEntry c2me$visitIfAbsent(CompiledEntry entry, ArgumentVisitor visitor) {
        return this.backingCache.computeIfAbsent(entry, compiledEntry -> compiledEntry.newInstance(compiledEntry.getArgs(), visitor));
    }
}
