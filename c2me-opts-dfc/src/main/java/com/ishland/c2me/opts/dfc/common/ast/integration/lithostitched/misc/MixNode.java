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

package com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;

import java.util.Objects;

public class MixNode implements AstNode {

    public final AstNode input;
    public final AstNode argument1;
    public final AstNode argument2;

    public MixNode(AstNode input, AstNode argument1, AstNode argument2) {
        this.input = Objects.requireNonNull(input);
        this.argument1 = Objects.requireNonNull(argument1);
        this.argument2 = Objects.requireNonNull(argument2);
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{this.input, this.argument1, this.argument2};
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode input = this.input.transform(transformer);
        AstNode argument1 = this.argument1.transform(transformer);
        AstNode argument2 = this.argument2.transform(transformer);
        if (this.input == input && this.argument1 == argument1 && this.argument2 == argument2) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new MixNode(input, argument1, argument2));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MixNode that = (MixNode) o;
        return Objects.equals(input, that.input) && Objects.equals(argument1, that.argument1) && Objects.equals(argument2, that.argument2);
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.input.hashCode();
        result = 31 * result + this.argument1.hashCode();
        result = 31 * result + this.argument2.hashCode();

        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MixNode that = (MixNode) o;
        return input.relaxedEquals(that.input) && argument1.relaxedEquals(that.argument1) && argument2.relaxedEquals(that.argument2);
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.input.relaxedHashCode();
        result = 31 * result + this.argument1.relaxedHashCode();
        result = 31 * result + this.argument2.relaxedHashCode();

        return result;
    }
}
