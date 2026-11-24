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

package com.ishland.c2me.opts.worldgen.vanilla.common.biome_search_tree;

import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilSearchTree;
import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilSearchTreeTreeBranchNode;
import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilSearchTreeTreeLeafNode;
import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilSearchTreeTreeNode;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class FlatBiomeSearchTree<T> {

    private static final int NODE_STRIDE = 16;

    private final long[] nodes;
    private final Object[] leafValues;
    private final int stackDepth;
    private final ThreadLocal<SearchScratch> threadScratch;

    private FlatBiomeSearchTree(long[] nodes, Object[] leafValues, int stackDepth) {
        this.nodes = nodes;
        this.leafValues = leafValues;
        this.stackDepth = stackDepth;
        this.threadScratch = ThreadLocal.withInitial(() -> new SearchScratch(this.stackDepth));
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable FlatBiomeSearchTree<T> fromSearchTree(@Nullable MultiNoiseUtil.SearchTree<T> searchTree) {
        if (searchTree == null) {
            return null;
        }
        MultiNoiseUtil.SearchTree.TreeNode<T> root = ((IMultiNoiseUtilSearchTree<T>) (Object) searchTree).getFirstNode();
        return fromRoot(root);
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable FlatBiomeSearchTree<T> fromRoot(@Nullable MultiNoiseUtil.SearchTree.TreeNode<T> root) {
        if (root == null) {
            return null;
        }

        List<MultiNoiseUtil.SearchTree.TreeNode<T>> orderedNodes = new ArrayList<>();
        List<Integer> childStarts = new ArrayList<>();
        List<Integer> childCounts = new ArrayList<>();
        ArrayDeque<MultiNoiseUtil.SearchTree.TreeNode<T>> queue = new ArrayDeque<>();
        ArrayDeque<Integer> indexQueue = new ArrayDeque<>();
        ArrayDeque<Integer> depthQueue = new ArrayDeque<>();

        orderedNodes.add(root);
        childStarts.add(0);
        childCounts.add(0);
        queue.add(root);
        indexQueue.add(0);
        depthQueue.add(0);

        int maxStackDepth = 1;
        while (!queue.isEmpty()) {
            MultiNoiseUtil.SearchTree.TreeNode<T> current = queue.poll();
            int curIdx = indexQueue.poll();
            int depth = depthQueue.poll();

            if (current instanceof MultiNoiseUtil.SearchTree.TreeBranchNode<T> branchNode) {
                MultiNoiseUtil.SearchTree.TreeNode<T>[] subTree =
                        ((IMultiNoiseUtilSearchTreeTreeBranchNode<T>) (Object) branchNode).getSubTree();
                if (subTree == null) {
                    return null;
                }
                int start = orderedNodes.size();
                childStarts.set(curIdx, start);
                childCounts.set(curIdx, subTree.length);
                if (subTree.length > 0 && depth + 1 > maxStackDepth) {
                    maxStackDepth = depth + 1;
                }
                for (MultiNoiseUtil.SearchTree.TreeNode<T> child : subTree) {
                    if (child == null) {
                        return null;
                    }
                    int childIdx = orderedNodes.size();
                    orderedNodes.add(child);
                    childStarts.add(0);
                    childCounts.add(0);
                    queue.add(child);
                    indexQueue.add(childIdx);
                    depthQueue.add(depth + 1);
                }
            } else if (!(current instanceof MultiNoiseUtil.SearchTree.TreeLeafNode<?>)) {
                return null;
            }
        }

        int count = orderedNodes.size();
        if (count > Integer.MAX_VALUE / NODE_STRIDE) {
            return null;
        }

        long[] flat = new long[count * NODE_STRIDE];
        Object[] leafVals = new Object[count];

        for (int i = 0; i < count; i++) {
            MultiNoiseUtil.SearchTree.TreeNode<T> node = orderedNodes.get(i);
            MultiNoiseUtil.ParameterRange[] params = ((IMultiNoiseUtilSearchTreeTreeNode) (Object) node).getParameters();
            if (params == null || params.length != 7) {
                return null;
            }
            int base = i << 4;
            for (int d = 0; d < 7; d++) {
                MultiNoiseUtil.ParameterRange range = params[d];
                if (range == null) {
                    return null;
                }
                flat[base + (d << 1)] = range.min();
                flat[base + (d << 1) + 1] = range.max();
            }

            if (node instanceof MultiNoiseUtil.SearchTree.TreeLeafNode<T> leafNode) {
                flat[base + 14] = -1L;
                leafVals[i] = ((IMultiNoiseUtilSearchTreeTreeLeafNode<T>) (Object) leafNode).getValue();
            } else {
                long cStart = childStarts.get(i) & 0xFFFF_FFFFL;
                long cCount = childCounts.get(i) & 0xFFFF_FFFFL;
                flat[base + 14] = cStart | (cCount << 32);
            }
        }

        return new FlatBiomeSearchTree<>(flat, leafVals, maxStackDepth);
    }

    public T search(MultiNoiseUtil.NoiseValuePoint point) {
        return search(
                point.temperatureNoise(),
                point.humidityNoise(),
                point.continentalnessNoise(),
                point.erosionNoise(),
                point.depth(),
                point.weirdnessNoise()
        );
    }

    @SuppressWarnings("unchecked")
    public T search(long q0, long q1, long q2, long q3, long q4, long q5) {
        long[] nodes = this.nodes;
        long rootMeta = nodes[14];
        if (rootMeta < 0L) {
            return (T) this.leafValues[0];
        }

        SearchScratch scratch = this.threadScratch.get();
        int bestLeaf = scratch.lastLeafNodeIdx;
        long bestDist = Long.MAX_VALUE;
        if (bestLeaf >= 0) {
            bestDist = nodeDistSq(nodes, bestLeaf << 4, q0, q1, q2, q3, q4, q5);
            if (bestDist == 0L) {
                return (T) this.leafValues[bestLeaf];
            }
        }

        int rootStart = (int) rootMeta;
        int rootCount = (int) (rootMeta >>> 32);
        if (rootCount <= 0) {
            return bestLeaf >= 0 ? (T) this.leafValues[bestLeaf] : null;
        }

        int[] stackCur = scratch.stackCur;
        int[] stackEnd = scratch.stackEnd;
        stackCur[0] = rootStart;
        stackEnd[0] = rootStart + rootCount;
        int sp = 1;

        while (sp > 0) {
            int top = sp - 1;
            int cur = stackCur[top];
            int end = stackEnd[top];
            if (cur >= end) {
                sp = top;
                continue;
            }
            stackCur[top] = cur + 1;
            int base = cur << 4;
            long d = nodeDistSq(nodes, base, q0, q1, q2, q3, q4, q5);
            if (d < bestDist) {
                long meta = nodes[base + 14];
                if (meta < 0L) {
                    bestDist = d;
                    bestLeaf = cur;
                    if (bestDist == 0L) {
                        break;
                    }
                } else {
                    int cStart = (int) meta;
                    int cCount = (int) (meta >>> 32);
                    if (cCount > 0) {
                        stackCur[sp] = cStart;
                        stackEnd[sp] = cStart + cCount;
                        sp++;
                    }
                }
            }
        }

        scratch.lastLeafNodeIdx = bestLeaf;
        return bestLeaf >= 0 ? (T) this.leafValues[bestLeaf] : null;
    }

    private static long nodeDistSq(
            long[] nodes,
            int base,
            long q0,
            long q1,
            long q2,
            long q3,
            long q4,
            long q5
    ) {
        long d0 = axisDist(nodes[base], nodes[base + 1], q0);
        long d1 = axisDist(nodes[base + 2], nodes[base + 3], q1);
        long d2 = axisDist(nodes[base + 4], nodes[base + 5], q2);
        long d3 = axisDist(nodes[base + 6], nodes[base + 7], q3);
        long d4 = axisDist(nodes[base + 8], nodes[base + 9], q4);
        long d5 = axisDist(nodes[base + 10], nodes[base + 11], q5);
        long d6 = axisDist(nodes[base + 12], nodes[base + 13], 0L);
        return d0 * d0 + d1 * d1 + d2 * d2 + d3 * d3 + d4 * d4 + d5 * d5 + d6 * d6;
    }

    private static long axisDist(long min, long max, long q) {
        long above = q - max;
        if (above > 0L) {
            return above;
        }
        long below = min - q;
        return Math.max(below, 0L);
    }

    private static final class SearchScratch {
        int lastLeafNodeIdx = -1;
        final int[] stackCur;
        final int[] stackEnd;

        SearchScratch(int stackDepth) {
            this.stackCur = new int[stackDepth];
            this.stackEnd = new int[stackDepth];
        }
    }

}
