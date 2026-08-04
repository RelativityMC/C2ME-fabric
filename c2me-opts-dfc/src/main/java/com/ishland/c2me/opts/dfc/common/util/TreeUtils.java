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

package com.ishland.c2me.opts.dfc.common.util;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxShortNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinShortNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MulNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.EndIslandsNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.IntervalSelectNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceNode;
import com.ishland.c2me.opts.dfc.common.ast.noise.GenericShiftedNoiseNode;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineNormalNode;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Collection;
import java.util.function.Consumer;

public class TreeUtils {

    private static void enumerate(AstNode node, AstNodeConsumer consumer) {
        if (consumer.accept(node) == AstNodeConsumer.IterationBehavior.CONTINUE) {
            for (AstNode child : node.getChildren()) {
                enumerate(child, consumer);
            }
        }
    }

    public static boolean isNonTrivial(AstNode node) {
        return node instanceof GenericShiftedNoiseNode ||
                node instanceof EndIslandsNode;
    }

    public static boolean isBranch(AstNode node) {
        return node instanceof MaxShortNode ||
                node instanceof MinShortNode ||
                node instanceof MulNode ||
                node instanceof IntervalSelectNode ||
                node instanceof RangeChoiceNode ||
                node instanceof SplineNormalNode;
    }

    public static boolean hasNonTrivialChildrenUntilBranch(AstNode node) {
        boolean[] result = new boolean[1];
        enumerate(node, node1 -> {
            if (isBranch(node1)) return AstNodeConsumer.IterationBehavior.STOP_EXPANDING;
            if (!result[0] && isNonTrivial(node1)) {
                result[0] = true;
            }
            return result[0] ? AstNodeConsumer.IterationBehavior.STOP_EXPANDING : AstNodeConsumer.IterationBehavior.CONTINUE;
        });
        return result[0];
    }

    private static void enumerateUntilNonTrivialBranch(AstNode node, Consumer<AstNode> consumer) {
        enumerate(node, node1 -> {
            consumer.accept(node1);
            if (isBranch(node1) && hasNonTrivialChildrenUntilBranch(node1)) {
                return AstNodeConsumer.IterationBehavior.STOP_EXPANDING;
            }
            return AstNodeConsumer.IterationBehavior.CONTINUE;
        });
    }

    public static Collection<AstNode> findLargestCommonSubtrees(AstNode... roots) {
        if (roots.length < 2) throw new IllegalArgumentException("Cannot find largest common subtrees with less than 2 roots");

        // collect all nodes
        ObjectLinkedOpenHashSet<AstNode> commonNodes = new ObjectLinkedOpenHashSet<>();
        enumerateUntilNonTrivialBranch(roots[0], k -> commonNodes.add(k));

        // find common nodes
        {
            ObjectOpenHashSet<AstNode> tmp = new ObjectOpenHashSet<>();
            for (int i = 1, rootsLength = roots.length; i < rootsLength; i++) {
                AstNode root = roots[i];
                enumerateUntilNonTrivialBranch(root, tmp::add);
                commonNodes.retainAll(tmp);
                tmp.clear();
            }
        }

        // remove subtrees
        ObjectLinkedOpenHashSet<AstNode> toRemove = new ObjectLinkedOpenHashSet<>();
        for (AstNode node : commonNodes) {
            enumerateUntilNonTrivialBranch(node, node1 -> {
                if (node != node1 && commonNodes.contains(node1)) {
                    toRemove.add(node1);
                }
            });
        }
        commonNodes.removeAll(toRemove);

        return commonNodes;
    }

    public static interface AstNodeConsumer {

        /**
         * Visits a {@link AstNode}
         * @param node the node
         * @return whether to continue expanding into this node
         */
        public IterationBehavior accept(AstNode node);

        public enum IterationBehavior {
            CONTINUE,
            STOP_EXPANDING,
            ;
        }
    }

}
