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

package com.ishland.c2me.notickvd.common.iterators;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestIterators {

    private static final int RADIUS = 256;

    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    public void testSpiral() {
        SpiralIterator iterator = new SpiralIterator(0, 0, RADIUS);
        ObjectSet<ChunkPos> chunks = new ObjectLinkedOpenHashSet<>((int) iterator.total());
        while (iterator.hasNext()) {
            chunks.add(iterator.next());
        }
        if (iterator.total() != chunks.size()) {
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    if (!chunks.contains(new ChunkPos(x, z))) {
                        System.out.println(String.format("%d %d", x, z));
                    }
                }
            }
            Assertions.fail();
        }
        for (ChunkPos chunk : chunks) {
            Assertions.assertTrue(iterator.isInRange(chunk.x(), chunk.z()));
        }
    }

    @AfterAll
    public static void teardown() {
        Util.shutdownExecutors();
    }

}
