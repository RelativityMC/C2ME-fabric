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

public class RepositionNode implements AstNode {
    public final AstNode input;
    public final AstNode inputX;
    public final AstNode inputY;
    public final AstNode inputZ;

    public RepositionNode(AstNode input, AstNode inputX, AstNode inputY, AstNode inputZ) {
        this.input = Objects.requireNonNull(input);
        this.inputX = Objects.requireNonNull(inputX);
        this.inputY = Objects.requireNonNull(inputY);
        this.inputZ = Objects.requireNonNull(inputZ);
        this.assertType();
    }

    public void assertType() {
        Assertions.assertTrue(this.inputX.getReturnType() == ReturnType.F64, "inputX isn't F64: %s", this.inputX.getReturnType());
        Assertions.assertTrue(this.inputY.getReturnType() == ReturnType.F64, "inputY isn't F64: %s", this.inputY.getReturnType());
        Assertions.assertTrue(this.inputZ.getReturnType() == ReturnType.F64, "inputZ isn't F64: %s", this.inputZ.getReturnType());
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[] { this.input, this.inputX, this.inputY, this.inputZ };
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode input = this.input.transform(transformer);
        AstNode inputX = this.inputX.transform(transformer);
        AstNode inputY = this.inputY.transform(transformer);
        AstNode inputZ = this.inputZ.transform(transformer);
        if (input == this.input && inputX == this.inputX && inputY == this.inputY && inputZ == this.inputZ) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new RepositionNode(input, inputX, inputY, inputZ));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RepositionNode that = (RepositionNode) o;
        return input.equals(that.input) && inputX.equals(that.inputX) && inputY.equals(that.inputY) && inputZ.equals(that.inputZ);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + input.hashCode();
        result = 31 * result + inputX.hashCode();
        result = 31 * result + inputY.hashCode();
        result = 31 * result + inputZ.hashCode();
        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (o == null || getClass() != o.getClass()) return false;
        RepositionNode that = (RepositionNode) o;
        return input.relaxedEquals(that.input) && inputX.relaxedEquals(that.inputX) && inputY.relaxedEquals(that.inputY) && inputZ.relaxedEquals(that.inputZ);
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;
        result = 31 * result + input.relaxedHashCode();
        result = 31 * result + inputX.relaxedHashCode();
        result = 31 * result + inputY.relaxedHashCode();
        result = 31 * result + inputZ.relaxedHashCode();
        return result;
    }

    @Override
    public ReturnType getReturnType() {
        return this.input.getReturnType();
    }
}
