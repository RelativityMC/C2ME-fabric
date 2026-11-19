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

package com.ishland.c2me.opts.dfc.common.ast.unary;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;

import java.util.Objects;

public abstract class AbstractUnaryNode implements AstNode {

    public final AstNode operand;

    public AbstractUnaryNode(AstNode operand) {
        this.operand = Objects.requireNonNull(operand);
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{operand};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractUnaryNode that = (AbstractUnaryNode) o;
        return Objects.equals(operand, that.operand);
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + operand.hashCode();

        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractUnaryNode that = (AbstractUnaryNode) o;
        return operand.relaxedEquals(that.operand);
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + operand.relaxedHashCode();

        return result;
    }

    protected abstract AstNode newInstance(AstNode operand);

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode operand = this.operand.transform(transformer);
        if (this.operand == operand) {
            return transformer.transform(this);
        } else {
            return transformer.transform(newInstance(operand));
        }
    }

}
