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

import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilSearchTreeTreeNode;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class TestFlatBiomeSearchTree {

    private static <T> long vanillaDistance(MultiNoiseUtil.SearchTree.TreeNode<T> node, long[] target) {
        MultiNoiseUtil.ParameterRange[] params = ((IMultiNoiseUtilSearchTreeTreeNode) (Object) node).getParameters();
        long sum = 0L;
        for (int i = 0; i < 7; i++) {
            long d = params[i].getDistance(target[i]);
            sum += d * d;
        }
        return sum;
    }

    private static List<Pair<MultiNoiseUtil.NoiseHypercube, String>> buildRandomEntries(
            SplittableRandom rng,
            int count,
            long coordSpan,
            long widthSpan
    ) {
        List<Pair<MultiNoiseUtil.NoiseHypercube, String>> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            MultiNoiseUtil.ParameterRange[] ranges = new MultiNoiseUtil.ParameterRange[6];
            for (int d = 0; d < 6; d++) {
                long center = rng.nextLong(-coordSpan, coordSpan + 1L);
                long halfWidth = rng.nextLong(0L, widthSpan + 1L);
                ranges[d] = new MultiNoiseUtil.ParameterRange(center - halfWidth, center + halfWidth);
            }
            long offset = rng.nextLong(0L, Math.max(1L, widthSpan));
            MultiNoiseUtil.NoiseHypercube cube = new MultiNoiseUtil.NoiseHypercube(
                    ranges[0], ranges[1], ranges[2], ranges[3], ranges[4], ranges[5], offset
            );
            entries.add(Pair.of(cube, "biome_" + i));
        }
        return entries;
    }

    private static void assertParity(
            List<Pair<MultiNoiseUtil.NoiseHypercube, String>> entries,
            int maxChildren,
            long querySpan,
            int queries,
            long seed
    ) {
        MultiNoiseUtil.SearchTree<String> vanillaTree = MultiNoiseUtil.SearchTree.create(entries, maxChildren);
        FlatBiomeSearchTree<String> flatTree = FlatBiomeSearchTree.fromSearchTree(vanillaTree);
        Assertions.assertNotNull(flatTree);

        MultiNoiseUtil.Entries<String> wrappedEntries = new MultiNoiseUtil.Entries<>(entries).create(maxChildren);

        SplittableRandom rng = new SplittableRandom(seed);
        long q0 = 0L, q1 = 0L, q2 = 0L, q3 = 0L, q4 = 0L, q5 = 0L;
        long step = Math.max(1L, querySpan / 25L);

        for (int i = 0; i < queries; i++) {
            if ((i & 7) == 0) {
                q0 = rng.nextLong(-querySpan, querySpan + 1L);
                q1 = rng.nextLong(-querySpan, querySpan + 1L);
                q2 = rng.nextLong(-querySpan, querySpan + 1L);
                q3 = rng.nextLong(-querySpan, querySpan + 1L);
                q4 = rng.nextLong(-querySpan, querySpan + 1L);
                q5 = rng.nextLong(-querySpan, querySpan + 1L);
            } else {
                q0 += rng.nextLong(-step, step + 1L);
                q1 += rng.nextLong(-step, step + 1L);
                q2 += rng.nextLong(-step, step + 1L);
                q3 += rng.nextLong(-step, step + 1L);
                q4 += rng.nextLong(-step, step + 1L);
                q5 += rng.nextLong(-step, step + 1L);
            }

            MultiNoiseUtil.NoiseValuePoint point = new MultiNoiseUtil.NoiseValuePoint(q0, q1, q2, q3, q4, q5);
            String expected = vanillaTree.get(point, TestFlatBiomeSearchTree::vanillaDistance);
            String actualFlat = flatTree.search(point);
            String actualEntries = wrappedEntries.getValue(point);

            Assertions.assertEquals(expected, actualFlat, "FlatBiomeSearchTree mismatch at query " + i);
            Assertions.assertEquals(expected, actualEntries, "Entries.getValue mismatch at query " + i);
        }
    }

    @Test
    public void testVanillaRangeParity50k() {
        List<Pair<MultiNoiseUtil.NoiseHypercube, String>> entries =
                buildRandomEntries(new SplittableRandom(0x1234_5678L), 250, 15_000L, 4_000L);
        assertParity(entries, 19, 20_000L, 50_000, 0xCAFE_BABEL);
    }

    @Test
    public void testModdedWide64BitRangeParity50k() {
        List<Pair<MultiNoiseUtil.NoiseHypercube, String>> entries =
                buildRandomEntries(new SplittableRandom(0x9E37_79B9L), 500, 150_000L, 25_000L);
        assertParity(entries, 19, 200_000L, 50_000, 0xDEAD_BEEFL);
        assertParity(entries, 6, 200_000L, 10_000, 0xFACE_FEEDL);
    }

    @Test
    public void testTieBreakingAndThreadLocalHintParity() {
        MultiNoiseUtil.ParameterRange zero = new MultiNoiseUtil.ParameterRange(0L, 0L);
        MultiNoiseUtil.ParameterRange band = new MultiNoiseUtil.ParameterRange(-50_000L, 50_000L);
        MultiNoiseUtil.ParameterRange left = new MultiNoiseUtil.ParameterRange(-100_000L, -10_000L);
        MultiNoiseUtil.ParameterRange right = new MultiNoiseUtil.ParameterRange(10_000L, 100_000L);

        List<Pair<MultiNoiseUtil.NoiseHypercube, String>> entries = List.of(
                Pair.of(new MultiNoiseUtil.NoiseHypercube(left, zero, zero, zero, zero, zero, 0L), "left_a"),
                Pair.of(new MultiNoiseUtil.NoiseHypercube(right, zero, zero, zero, zero, zero, 0L), "right_a"),
                Pair.of(new MultiNoiseUtil.NoiseHypercube(band, zero, zero, zero, zero, zero, 10_000L), "center_offset_1"),
                Pair.of(new MultiNoiseUtil.NoiseHypercube(band, zero, zero, zero, zero, zero, 10_000L), "center_offset_2")
        );

        MultiNoiseUtil.SearchTree<String> vanillaTree = MultiNoiseUtil.SearchTree.create(entries, 19);
        FlatBiomeSearchTree<String> flatTree = FlatBiomeSearchTree.fromSearchTree(vanillaTree);
        Assertions.assertNotNull(flatTree);

        MultiNoiseUtil.NoiseValuePoint[] sequence = new MultiNoiseUtil.NoiseValuePoint[]{
                new MultiNoiseUtil.NoiseValuePoint(0L, 0L, 0L, 0L, 0L, 0L),
                new MultiNoiseUtil.NoiseValuePoint(50_000L, 0L, 0L, 0L, 0L, 0L),
                new MultiNoiseUtil.NoiseValuePoint(0L, 0L, 0L, 0L, 0L, 0L),
                new MultiNoiseUtil.NoiseValuePoint(-50_000L, 0L, 0L, 0L, 0L, 0L),
                new MultiNoiseUtil.NoiseValuePoint(0L, 0L, 0L, 0L, 0L, 0L),
                new MultiNoiseUtil.NoiseValuePoint(100_000L, 0L, 0L, 0L, 0L, 0L),
                new MultiNoiseUtil.NoiseValuePoint(0L, 0L, 0L, 0L, 0L, 0L)
        };

        for (int i = 0; i < sequence.length; i++) {
            String expected = vanillaTree.get(sequence[i], TestFlatBiomeSearchTree::vanillaDistance);
            String actual = flatTree.search(sequence[i]);
            Assertions.assertEquals(expected, actual, "Tie-break mismatch at step " + i);
        }
    }

    @Test
    public void testSingleLeafAndNullSafety() {
        Assertions.assertNull(FlatBiomeSearchTree.fromSearchTree(null));
        Assertions.assertNull(FlatBiomeSearchTree.fromRoot(null));

        MultiNoiseUtil.ParameterRange range = new MultiNoiseUtil.ParameterRange(-100_000L, 100_000L);
        List<Pair<MultiNoiseUtil.NoiseHypercube, String>> single = List.of(
                Pair.of(new MultiNoiseUtil.NoiseHypercube(range, range, range, range, range, range, 0L), "only_biome")
        );
        MultiNoiseUtil.SearchTree<String> vanillaTree = MultiNoiseUtil.SearchTree.create(single, 19);
        FlatBiomeSearchTree<String> flatTree = FlatBiomeSearchTree.fromSearchTree(vanillaTree);
        Assertions.assertNotNull(flatTree);

        MultiNoiseUtil.NoiseValuePoint point = new MultiNoiseUtil.NoiseValuePoint(500_000L, -500_000L, 0L, 0L, 0L, 0L);
        Assertions.assertEquals("only_biome", flatTree.search(point));
    }

    @Test
    public void testMixinAppliesAndPreservesCustomSubclasses() throws Exception {
        MultiNoiseUtil.ParameterRange range = new MultiNoiseUtil.ParameterRange(-100_000L, 100_000L);
        List<Pair<MultiNoiseUtil.NoiseHypercube, String>> entries = List.of(
                Pair.of(new MultiNoiseUtil.NoiseHypercube(range, range, range, range, range, range, 0L), "base")
        );

        MultiNoiseUtil.Entries<String> standard = new MultiNoiseUtil.Entries<>(entries);
        Field field = MultiNoiseUtil.Entries.class.getDeclaredField("c2me$flatSearchTree");
        field.setAccessible(true);
        Assertions.assertNotNull(field.get(standard), "MixinMultiNoiseUtilEntries should populate c2me$flatSearchTree");

        MultiNoiseUtil.Entries<String> subclass = new MultiNoiseUtil.Entries<>(entries) {
            @Override
            protected String getValue(
                    MultiNoiseUtil.NoiseValuePoint point,
                    MultiNoiseUtil.NodeDistanceFunction<String> distanceFunction
            ) {
                return "custom_override";
            }
        };
        Assertions.assertNull(field.get(subclass), "Custom Entries subclasses must retain their getValue override");
        Assertions.assertEquals("custom_override", subclass.getValue(new MultiNoiseUtil.NoiseValuePoint(0L, 0L, 0L, 0L, 0L, 0L)));
    }

}
