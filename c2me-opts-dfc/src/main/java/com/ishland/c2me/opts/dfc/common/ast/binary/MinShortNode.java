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

package com.ishland.c2me.opts.dfc.common.ast.binary;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;

public class MinShortNode extends AbstractBinaryNode {

    public final double rightMin;

    public MinShortNode(AstNode left, AstNode right, double rightMin) {
        super(left, right);
        this.rightMin = rightMin;
    }

    @Override
    protected AstNode newInstance(AstNode left, AstNode right) {
        return new MinShortNode(left, right, this.rightMin);
    }

    @Override
    public boolean canSwapOperandsSafely() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MinShortNode that = (MinShortNode) o;
        return Double.compare(this.rightMin, that.rightMin) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Double.hashCode(this.rightMin);
        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.relaxedEquals(o)) return false;
        MinShortNode that = (MinShortNode) o;
        return Double.compare(this.rightMin, that.rightMin) == 0;
    }

    @Override
    public int relaxedHashCode() {
        int result = super.relaxedHashCode();
        result = 31 * result + Double.hashCode(this.rightMin);
        return result;
    }

}
