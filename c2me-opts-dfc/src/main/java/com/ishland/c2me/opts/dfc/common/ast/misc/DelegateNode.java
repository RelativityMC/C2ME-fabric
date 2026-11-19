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

package com.ishland.c2me.opts.dfc.common.ast.misc;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Objects;

public class DelegateNode implements AstNode {

//    private static final ConcurrentHashMap<Class<?>, LongAdder> statistics = new ConcurrentHashMap<>();

    private final DensityFunction densityFunction;

    public DelegateNode(DensityFunction densityFunction) {
        this.densityFunction = Objects.requireNonNull(densityFunction);
//        statistics.computeIfAbsent(densityFunction.getClass(), unused -> new LongAdder()).increment();
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[0];
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        return transformer.transform(this);
    }

    public DensityFunction getDelegate() {
        return this.densityFunction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DelegateNode that = (DelegateNode) o;
        return Objects.equals(densityFunction, that.densityFunction);
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + Objects.hashCode(this.getClass());
        result = 31 * result + Objects.hashCode(densityFunction);

        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DelegateNode that = (DelegateNode) o;
        return densityFunction.getClass() == that.densityFunction.getClass();
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;

        result = 31 * result + Objects.hashCode(this.getClass());
        result = 31 * result + Objects.hashCode(densityFunction.getClass());

        return result;
    }
}
