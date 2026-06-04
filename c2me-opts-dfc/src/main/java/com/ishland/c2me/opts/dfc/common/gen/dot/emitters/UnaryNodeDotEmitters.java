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

package com.ishland.c2me.opts.dfc.common.gen.dot.emitters;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.AbsNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.AbstractUnaryNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.CubeNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.NegMulNode;
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
        registry.registerExactMatch(CubeNode.class, new UnaryNodeEmitter<>(node -> "cube"));
        registry.registerExactMatch(NegMulNode.class, new UnaryNodeEmitter<>(node -> "NegMul" + node.negMul));
        registry.registerExactMatch(SquareNode.class, new UnaryNodeEmitter<>(node -> "square"));
        registry.registerExactMatch(SqueezeNode.class, new UnaryNodeEmitter<>(node -> "squeeze"));
    }

}
