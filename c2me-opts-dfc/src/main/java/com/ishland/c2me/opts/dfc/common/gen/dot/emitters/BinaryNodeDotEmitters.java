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
