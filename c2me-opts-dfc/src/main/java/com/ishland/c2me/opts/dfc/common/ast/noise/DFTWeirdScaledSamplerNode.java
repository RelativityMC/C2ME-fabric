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

package com.ishland.c2me.opts.dfc.common.ast.noise;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

import java.util.Objects;

public class DFTWeirdScaledSamplerNode implements AstNode {

    public final AstNode input;
    public final DensityFunction.Noise noise;
    public final DensityFunctionTypes.WeirdScaledSampler.RarityValueMapper mapper;

    public DFTWeirdScaledSamplerNode(AstNode input, DensityFunction.Noise noise, DensityFunctionTypes.WeirdScaledSampler.RarityValueMapper mapper) {
        this.input = Objects.requireNonNull(input);
        this.noise = Objects.requireNonNull(noise);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{this.input};
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode input = this.input.transform(transformer);
        if (input == this.input) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new DFTWeirdScaledSamplerNode(input, this.noise, this.mapper));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DFTWeirdScaledSamplerNode that = (DFTWeirdScaledSamplerNode) o;
        return Objects.equals(input, that.input) && Objects.equals(noise, that.noise) && mapper == that.mapper;
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + input.hashCode();
        result = 31 * result + noise.hashCode();
        result = 31 * result + mapper.hashCode();

        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DFTWeirdScaledSamplerNode that = (DFTWeirdScaledSamplerNode) o;
        return input.relaxedEquals(that.input) && Objects.equals(noise, that.noise) && mapper == that.mapper;
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + input.relaxedHashCode();
        result = 31 * result + noise.hashCode();
        result = 31 * result + mapper.hashCode();

        return result;
    }
}
