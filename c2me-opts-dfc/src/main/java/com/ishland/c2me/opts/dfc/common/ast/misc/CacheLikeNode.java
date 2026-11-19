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
import com.ishland.c2me.opts.dfc.common.ducks.IFastCacheLike;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

import java.util.Objects;

public class CacheLikeNode implements AstNode {

    private final IFastCacheLike cacheLike;
    private final AstNode delegate;

    public CacheLikeNode(IFastCacheLike cacheLike, AstNode delegate) {
        this.cacheLike = cacheLike;
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{this.delegate};
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode delegate = this.delegate.transform(transformer);
        if (this.delegate == delegate) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new CacheLikeNode(this.cacheLike, delegate));
        }
    }

    public IFastCacheLike getCacheLike() {
        return cacheLike;
    }

    public AstNode getDelegate() {
        return delegate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheLikeNode that = (CacheLikeNode) o;
        return equals(cacheLike, that.cacheLike) && Objects.equals(delegate, that.delegate);
    }

    private static boolean equals(IFastCacheLike a, IFastCacheLike b) {
        if ((Object) a instanceof DensityFunctionTypes.Wrapping wrappingA && (Object) b instanceof DensityFunctionTypes.Wrapping wrappingB) {
            return wrappingA.type() == wrappingB.type();
        } else {
            return a.equals(b);
        }
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + hashCode(cacheLike);
        result = 31 * result + delegate.hashCode();

        return result;
    }

    private static int hashCode(IFastCacheLike o) {
        if ((Object) o instanceof DensityFunctionTypes.Wrapping wrapping) {
            return wrapping.type().hashCode();
        } else {
            return o.hashCode();
        }
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheLikeNode that = (CacheLikeNode) o;
        return relaxedEquals(cacheLike, that.cacheLike) && delegate.relaxedEquals(that.delegate);
    }

    private static boolean relaxedEquals(IFastCacheLike a, IFastCacheLike b) {
        if ((Object) a instanceof DensityFunctionTypes.Wrapping wrappingA && (Object) b instanceof DensityFunctionTypes.Wrapping wrappingB) {
            return wrappingA.type() == wrappingB.type();
        } else {
            return a.getClass() == b.getClass();
        }
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + relaxedHashCode(this.cacheLike);
        result = 31 * result + delegate.relaxedHashCode();

        return result;
    }

    private static int relaxedHashCode(IFastCacheLike o) {
        if ((Object) o instanceof DensityFunctionTypes.Wrapping wrapping) {
            return wrapping.type().hashCode();
        } else {
            return o.getClass().hashCode();
        }
    }
}
