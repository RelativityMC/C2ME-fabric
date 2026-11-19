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

import java.util.Arrays;

public class IntervalSelectNode implements AstNode {

    public final AstNode input;
    public final double[] thresholds;
    public final AstNode[] functions;

    public IntervalSelectNode(AstNode input, double[] thresholds, AstNode[] functions) {
        this.input = input;
        this.thresholds = thresholds;
        this.functions = functions;
    }

    @Override
    public AstNode[] getChildren() {
        AstNode[] nodes = new AstNode[this.functions.length + 1];
        nodes[0] = this.input;
        System.arraycopy(this.functions, 0, nodes, 1, this.functions.length);
        return nodes;
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        boolean changed = false;

        AstNode transformedInput = this.input.transform(transformer);
        if (transformedInput != this.input) changed |= true;

        AstNode[] transformedFunctions = this.functions.clone();
        for (int i = 0, transformedFunctionsLength = transformedFunctions.length; i < transformedFunctionsLength; i++) {
            AstNode transformedFunction = transformedFunctions[i];
            transformedFunctions[i] = transformedFunction.transform(transformer);
            if (transformedFunctions[i] != transformedFunction) changed |= true;
        }

        if (!changed) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new IntervalSelectNode(transformedInput, this.thresholds.clone(), transformedFunctions));
        }
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (o == null || getClass() != o.getClass()) return false;
        IntervalSelectNode that = (IntervalSelectNode) o;
        if (!this.input.relaxedEquals(that.input) || !Arrays.equals(this.thresholds, that.thresholds)) return false;
        if (this.functions == that.functions)
            return true;
        if (this.functions == null || that.functions == null)
            return false;
        int length = this.functions.length;
        if (that.functions.length != length)
            return false;

        for (int i = 0; i < length; i++) {
            AstNode e1 = this.functions[i];
            AstNode e2 = that.functions[i];
            if (!e1.relaxedEquals(e2))
                return false;
        }
        return true;
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;
        result = 31 * result + input.relaxedHashCode();
        result = 31 * result + Arrays.hashCode(thresholds);
        for (AstNode function : functions) {
            result = 31 * result + function.relaxedHashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IntervalSelectNode that = (IntervalSelectNode) o;
        if (!this.input.equals(that.input) || !Arrays.equals(this.thresholds, that.thresholds)) return false;
        if (this.functions == that.functions)
            return true;
        if (this.functions == null || that.functions == null)
            return false;
        int length = this.functions.length;
        if (that.functions.length != length)
            return false;

        for (int i = 0; i < length; i++) {
            AstNode e1 = this.functions[i];
            AstNode e2 = that.functions[i];
            if (!e1.equals(e2))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + input.hashCode();
        result = 31 * result + Arrays.hashCode(thresholds);
        for (AstNode function : functions) {
            result = 31 * result + function.hashCode();
        }
        return result;
    }
}
