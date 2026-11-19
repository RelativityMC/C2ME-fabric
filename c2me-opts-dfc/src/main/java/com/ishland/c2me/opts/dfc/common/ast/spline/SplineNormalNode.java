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

package com.ishland.c2me.opts.dfc.common.ast.spline;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;

import java.util.Arrays;
import java.util.Objects;

public class SplineNormalNode implements AstNode {

    public final AstNode locationFunction;
    public final float[] locations;
    public final AstNode[] values;
    public final float[] derivatives;

    public SplineNormalNode(AstNode locationFunction, float[] locations, AstNode[] values, float[] derivatives) {
        this.locationFunction = Objects.requireNonNull(locationFunction);
        this.locations = Objects.requireNonNull(locations);
        this.values = Objects.requireNonNull(values);
        this.derivatives = Objects.requireNonNull(derivatives);
    }

    @Override
    public AstNode[] getChildren() {
        AstNode[] nodes = new AstNode[this.values.length + 1];
        nodes[0] = this.locationFunction;
        System.arraycopy(this.values, 0, nodes, 1, this.values.length);
        return nodes;
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        boolean changed = false;

        AstNode transformedLocationFunction = this.locationFunction.transform(transformer);
        if (transformedLocationFunction != this.locationFunction) changed |= true;

        AstNode[] transformedValues = this.values.clone();
        for (int i = 0, transformedValuesLength = transformedValues.length; i < transformedValuesLength; i++) {
            AstNode transformedFunction = transformedValues[i];
            transformedValues[i] = transformedFunction.transform(transformer);
            if (transformedValues[i] != transformedFunction) changed |= true;
        }

        if (!changed) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new SplineNormalNode(transformedLocationFunction, this.locations.clone(), transformedValues, this.derivatives.clone()));
        }
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (o == null || getClass() != o.getClass()) return false;
        SplineNormalNode that = (SplineNormalNode) o;
        if (!locationFunction.relaxedEquals(that.locationFunction)) return false;
        if (!Arrays.equals(locations, that.locations)) return false;

        int length = this.values.length;
        if (that.values.length != length)
            return false;
        for (int i = 0; i < length; i++) {
            AstNode e1 = this.values[i];
            AstNode e2 = that.values[i];
            if (!e1.relaxedEquals(e2))
                return false;
        }

        if (!Arrays.equals(derivatives, that.derivatives)) return false;

        return true;
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;
        result = 31 * result + locationFunction.relaxedHashCode();
        result = 31 * result + Arrays.hashCode(locations);
        for (AstNode value : values) {
            result = 31 * result + value.relaxedHashCode();
        }
        result = 31 * result + Arrays.hashCode(derivatives);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SplineNormalNode that = (SplineNormalNode) o;
        if (!locationFunction.equals(that.locationFunction)) return false;
        if (!Arrays.equals(locations, that.locations)) return false;

        int length = this.values.length;
        if (that.values.length != length)
            return false;
        for (int i = 0; i < length; i++) {
            AstNode e1 = this.values[i];
            AstNode e2 = that.values[i];
            if (!e1.equals(e2))
                return false;
        }

        if (!Arrays.equals(derivatives, that.derivatives)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + locationFunction.hashCode();
        result = 31 * result + Arrays.hashCode(locations);
        for (AstNode value : values) {
            result = 31 * result + value.hashCode();
        }
        result = 31 * result + Arrays.hashCode(derivatives);
        return result;
    }

    @Override
    public ReturnType getReturnType() {
        return ReturnType.F32;
    }
}
