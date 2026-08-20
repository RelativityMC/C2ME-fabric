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
import com.ishland.flowsched.util.Assertions;

import java.util.Objects;

public class LerpNode implements AstNode {

    public final AstNode delta;
    public final AstNode start;
    public final AstNode end;
    private final ReturnType returnType;

    public LerpNode(AstNode delta, AstNode start, AstNode end) {
        this.delta = Objects.requireNonNull(delta);
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
        assertType();
        this.returnType = this.delta.getReturnType();
    }

    public void assertType() {
        Assertions.assertTrue(
                this.delta.getReturnType() == this.start.getReturnType() && this.start.getReturnType() == this.end.getReturnType(),
                "types aren't the same: delta=%s, start=%s, end=%s",
                this.delta.getReturnType(), this.start.getReturnType(), this.end.getReturnType()
        );
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{this.delta, this.start, this.end};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LerpNode that = (LerpNode) o;
        return Objects.equals(delta, that.delta) &&
                Objects.equals(start, that.start) &&
                Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + delta.hashCode();
        result = 31 * result + start.hashCode();
        result = 31 * result + end.hashCode();

        return result;
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode transformedDelta = this.delta.transform(transformer);
        AstNode transformedStart = this.start.transform(transformer);
        AstNode transformedEnd = this.end.transform(transformer);
        if (transformedDelta == this.delta && transformedStart == this.start && transformedEnd == this.end) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new LerpNode(transformedDelta, transformedStart, transformedEnd));
        }
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LerpNode that = (LerpNode) o;
        return delta.relaxedEquals(that.delta) &&
                start.relaxedEquals(that.start) &&
                end.relaxedEquals(that.end);
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + delta.relaxedHashCode();
        result = 31 * result + start.relaxedHashCode();
        result = 31 * result + end.relaxedHashCode();

        return result;
    }

    @Override
    public ReturnType getReturnType() {
        return this.returnType;
    }
}
