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

public class RoundingDFNode implements AstNode {

    public final RoundingUnaryOperation operation;
    public final AstNode input;
    public final AstNode multiple;
    private final ReturnType returnType;

    public RoundingDFNode(RoundingUnaryOperation operation, AstNode input, AstNode multiple) {
        this.operation = Objects.requireNonNull(operation);
        this.input = Objects.requireNonNull(input);
        this.multiple = Objects.requireNonNull(multiple);
        this.assertSameReturnType();
        this.returnType = this.input.getReturnType();
    }

    public void assertSameReturnType() {
        Assertions.assertTrue(this.input.getReturnType() == this.multiple.getReturnType(), "Operand type do not match: %s != %s", this.input.getReturnType(), this.multiple.getReturnType());
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{input, multiple};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoundingDFNode that = (RoundingDFNode) o;
        return Objects.equals(operation, that.operation) && Objects.equals(input, that.input) && Objects.equals(multiple, that.multiple);
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + operation.hashCode();
        result = 31 * result + input.hashCode();
        result = 31 * result + multiple.hashCode();

        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoundingDFNode that = (RoundingDFNode) o;
        return Objects.equals(operation, that.operation) && input.relaxedEquals(that.input) && multiple.relaxedEquals(that.multiple);
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + operation.hashCode();
        result = 31 * result + input.relaxedHashCode();
        result = 31 * result + multiple.relaxedHashCode();

        return result;
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode input = this.input.transform(transformer);
        AstNode multiple = this.multiple.transform(transformer);
        if (input == this.input && multiple == this.multiple) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new RoundingDFNode(this.operation, input, multiple));
        }
    }

    @Override
    public final ReturnType getReturnType() {
        return this.returnType;
    }

    public enum RoundingUnaryOperation {
        FLOOR,
        ROUND_HALF_UP,
        CEIL,
        ROUND_TOWARDS_ZERO,
        ;
    }

}
