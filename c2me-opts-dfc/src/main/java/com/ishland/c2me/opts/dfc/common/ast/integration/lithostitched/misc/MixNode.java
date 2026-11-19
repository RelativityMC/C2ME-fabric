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
