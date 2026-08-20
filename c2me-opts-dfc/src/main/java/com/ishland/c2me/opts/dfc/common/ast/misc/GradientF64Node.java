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

package com.ishland.c2me.opts.dfc.common.ast.misc;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import com.ishland.c2me.opts.dfc.common.ast.meta.Axis;
import com.ishland.c2me.opts.dfc.common.ast.meta.Tiling;

import java.util.Objects;

public class GradientF64Node implements AstNode {

    public final Axis axis;
    public final Tiling tiling;
    public final int fromCoord;
    public final int toCoord;
    public final double fromValue;
    public final double toValue;

    public GradientF64Node(Axis axis, Tiling tiling, int fromCoord, int toCoord, double fromValue, double toValue) {
        this.axis = Objects.requireNonNull(axis);
        this.tiling = Objects.requireNonNull(tiling);
        this.fromCoord = fromCoord;
        this.toCoord = toCoord;
        this.fromValue = fromValue;
        this.toValue = toValue;
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[0];
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GradientF64Node that = (GradientF64Node) o;
        return fromCoord == that.fromCoord && toCoord == that.toCoord && Double.compare(fromValue, that.fromValue) == 0 && Double.compare(toValue, that.toValue) == 0 && axis == that.axis && tiling == that.tiling;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + axis.hashCode();
        result = 31 * result + tiling.hashCode();
        result = 31 * result + Integer.hashCode(fromCoord);
        result = 31 * result + Integer.hashCode(toCoord);
        result = 31 * result + Double.hashCode(fromValue);
        result = 31 * result + Double.hashCode(toValue);
        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        return this.equals(o);
    }

    @Override
    public int relaxedHashCode() {
        return this.hashCode();
    }

    @Override
    public ReturnType getReturnType() {
        return ReturnType.F64;
    }

}
