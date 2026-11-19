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

import java.util.Objects;

public class FindTopSurfaceNode implements AstNode {

    public final AstNode density;
    public final AstNode upperBound;
    public final AstNode lowerBound;
    public final int cellHeight;

    public FindTopSurfaceNode(AstNode density, AstNode upperBound, AstNode lowerBound, int cellHeight) {
        this.density = Objects.requireNonNull(density);
        this.upperBound = Objects.requireNonNull(upperBound);
        this.lowerBound = Objects.requireNonNull(lowerBound);
        this.cellHeight = cellHeight;
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[] {this.density, this.upperBound, this.lowerBound};
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode density = this.density.transform(transformer);
        AstNode upperBound = this.upperBound.transform(transformer);
        AstNode lowerBound = this.lowerBound.transform(transformer);
        if (density == this.density && upperBound == this.upperBound && lowerBound == this.lowerBound) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new FindTopSurfaceNode(density, upperBound, lowerBound, this.cellHeight));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FindTopSurfaceNode that = (FindTopSurfaceNode) o;
        return cellHeight == that.cellHeight && Objects.equals(density, that.density) && Objects.equals(upperBound, that.upperBound) && Objects.equals(lowerBound, that.lowerBound);
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.density.hashCode();
        result = 31 * result + this.upperBound.hashCode();
        result = 31 * result + this.lowerBound.hashCode();
        result = 31 * result + Integer.hashCode(cellHeight);

        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (o == null || getClass() != o.getClass()) return false;
        FindTopSurfaceNode that = (FindTopSurfaceNode) o;
        return cellHeight == that.cellHeight && this.density.relaxedEquals(that.density) && this.upperBound.relaxedEquals(that.upperBound) && this.lowerBound.relaxedEquals(that.lowerBound);
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.density.relaxedHashCode();
        result = 31 * result + this.upperBound.relaxedHashCode();
        result = 31 * result + this.lowerBound.relaxedHashCode();
        result = 31 * result + Integer.hashCode(cellHeight);

        return result;
    }
}
