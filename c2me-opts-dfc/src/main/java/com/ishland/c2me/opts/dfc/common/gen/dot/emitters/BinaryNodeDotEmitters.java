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
import com.ishland.c2me.opts.dfc.common.ast.binary.AbstractBinaryNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.AddNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.DivNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxShortNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinShortNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MulNode;
import com.ishland.c2me.opts.dfc.common.gen.CodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.dot.DotEmitter;
import com.ishland.c2me.opts.dfc.common.gen.dot.DotGen;

import java.util.Objects;
import java.util.function.Function;

public class BinaryNodeDotEmitters {

    public static class BinaryNodeEmitter<T extends AbstractBinaryNode> implements DotEmitter<T> {
        private final Function<T, String> description;

        public BinaryNodeEmitter(Function<T, String> description) {
            this.description = Objects.requireNonNull(description);
        }

        @Override
        public int doDotGen(T node, DotGen.Context context, DotGen.Context.Builder builder) {
            return builder
                    .parallelogramShape()
                    .label(this.description.apply(node))
                    .edge(context.generate(node.left)).label("left").finish()
                    .edge(context.generate(node.right)).label("right").finish()
                    .build();
        }
    }

    public static void register(CodeGenRegistry<DotEmitter<? extends AstNode>> registry) {
        registry.registerExactMatch(AddNode.class, new BinaryNodeEmitter<>(node -> "add"));
        registry.registerExactMatch(DivNode.class, new BinaryNodeEmitter<>(node -> "div"));
        registry.registerExactMatch(MaxNode.class, new BinaryNodeEmitter<>(node -> "max"));
        registry.registerExactMatch(MaxShortNode.class, new BinaryNodeEmitter<>(node -> "max, shortcut rightMax=" + node.rightMax));
        registry.registerExactMatch(MinNode.class, new BinaryNodeEmitter<>(node -> "min"));
        registry.registerExactMatch(MinShortNode.class, new BinaryNodeEmitter<>(node -> "min, shortcut rightMin=" + node.rightMin));
        registry.registerExactMatch(MulNode.class, new BinaryNodeEmitter<>(node -> "mul"));
    }

}
