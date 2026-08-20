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
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Objects;

public class GenericShiftedF64NoiseNode implements AstNode {
    public final AstNode inputX;
    public final AstNode inputY;
    public final AstNode inputZ;
    public final DensityFunction.Noise noise;

    public GenericShiftedF64NoiseNode(AstNode inputX, AstNode inputY, AstNode inputZ, DensityFunction.Noise noise) {
        this.inputX = Objects.requireNonNull(inputX);
        this.inputY = Objects.requireNonNull(inputY);
        this.inputZ = Objects.requireNonNull(inputZ);
        this.noise = Objects.requireNonNull(noise);
        this.assertType();
    }

    public void assertType() {
        Assertions.assertTrue(this.inputX.getReturnType() == ReturnType.F64, "inputX isn't F64: %s", this.inputX.getReturnType());
        Assertions.assertTrue(this.inputY.getReturnType() == ReturnType.F64, "inputY isn't F64: %s", this.inputY.getReturnType());
        Assertions.assertTrue(this.inputZ.getReturnType() == ReturnType.F64, "inputZ isn't F64: %s", this.inputZ.getReturnType());
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[] { this.inputX, this.inputY, this.inputZ };
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode inputX = this.inputX.transform(transformer);
        AstNode inputY = this.inputY.transform(transformer);
        AstNode inputZ = this.inputZ.transform(transformer);
        if (inputX == this.inputX && inputY == this.inputY && inputZ == this.inputZ) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new GenericShiftedF64NoiseNode(inputX, inputY, inputZ, this.noise));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GenericShiftedF64NoiseNode that = (GenericShiftedF64NoiseNode) o;
        return inputX.equals(that.inputX) && inputY.equals(that.inputY) && inputZ.equals(that.inputZ) && noise.equals(that.noise);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + inputX.hashCode();
        result = 31 * result + inputY.hashCode();
        result = 31 * result + inputZ.hashCode();
        result = 31 * result + noise.hashCode();
        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (o == null || getClass() != o.getClass()) return false;
        GenericShiftedF64NoiseNode that = (GenericShiftedF64NoiseNode) o;
        return inputX.relaxedEquals(that.inputX) && inputY.relaxedEquals(that.inputY) && inputZ.relaxedEquals(that.inputZ) && noise.equals(that.noise);
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;
        result = 31 * result + inputX.relaxedHashCode();
        result = 31 * result + inputY.relaxedHashCode();
        result = 31 * result + inputZ.relaxedHashCode();
        result = 31 * result + noise.hashCode();
        return result;
    }

    @Override
    public ReturnType getReturnType() {
        return ReturnType.F64;
    }
}
