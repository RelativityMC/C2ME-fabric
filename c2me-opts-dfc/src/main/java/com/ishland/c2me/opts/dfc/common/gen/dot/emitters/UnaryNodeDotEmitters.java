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

package com.ishland.c2me.opts.dfc.common.gen.dot.emitters;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.AbsNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.AbstractUnaryNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.CeilNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.CosNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.CubeNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.FloorNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.NegMulNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SinNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SqrtNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SquareNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SqueezeNode;
import com.ishland.c2me.opts.dfc.common.gen.CodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.dot.DotEmitter;
import com.ishland.c2me.opts.dfc.common.gen.dot.DotGen;

import java.util.Objects;
import java.util.function.Function;

public class UnaryNodeDotEmitters {

    public static class UnaryNodeEmitter<T extends AbstractUnaryNode> implements DotEmitter<T> {
        private final Function<T, String> description;

        public UnaryNodeEmitter(Function<T, String> description) {
            this.description = Objects.requireNonNull(description);
        }

        @Override
        public int doDotGen(T node, DotGen.Context context, DotGen.Context.Builder builder) {
            return builder
                    .circleShape()
                    .label(this.description.apply(node))
                    .edge(context.generate(node.operand)).label("operand").finish()
                    .build();
        }
    }

    public static void register(CodeGenRegistry<DotEmitter<? extends AstNode>> registry) {
        registry.registerExactMatch(AbsNode.class, new UnaryNodeEmitter<>(node -> "abs"));
        registry.registerExactMatch(CeilNode.class, new UnaryNodeEmitter<>(node -> "ceil"));
        registry.registerExactMatch(CosNode.class, new UnaryNodeEmitter<>(node -> "cos"));
        registry.registerExactMatch(CubeNode.class, new UnaryNodeEmitter<>(node -> "cube"));
        registry.registerExactMatch(FloorNode.class, new UnaryNodeEmitter<>(node -> "floor"));
        registry.registerExactMatch(NegMulNode.class, new UnaryNodeEmitter<>(node -> "NegMul" + node.negMul));
        registry.registerExactMatch(SinNode.class, new UnaryNodeEmitter<>(node -> "sin"));
        registry.registerExactMatch(SqrtNode.class, new UnaryNodeEmitter<>(node -> "sqrt"));
        registry.registerExactMatch(SquareNode.class, new UnaryNodeEmitter<>(node -> "square"));
        registry.registerExactMatch(SqueezeNode.class, new UnaryNodeEmitter<>(node -> "squeeze"));
    }

}
