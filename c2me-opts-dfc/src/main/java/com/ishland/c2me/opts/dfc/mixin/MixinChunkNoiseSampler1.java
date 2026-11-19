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

package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.base.mixin.access.IChunkNoiseSampler;
import com.ishland.c2me.opts.dfc.common.ducks.IDfcObjectCacheCapable;
import com.ishland.c2me.opts.dfc.common.ducks.ICoordinatesFilling;
import com.ishland.c2me.opts.dfc.common.gen.jvm.util.DfcObjectCache;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/gen/chunk/ChunkNoiseSampler$1")
public class MixinChunkNoiseSampler1 implements IDfcObjectCacheCapable, ICoordinatesFilling {
    @Shadow
    @Final
    ChunkNoiseSampler field_36595;

    @Override
    public DfcObjectCache c2me$getDfcObjectCache() {
        return ((IDfcObjectCacheCapable) this.field_36595).c2me$getDfcObjectCache();
    }

    @Override
    public void c2me$fillCoordinates(int[] x, int[] y, int[] z) {
        for (int i = 0; i < ((IChunkNoiseSampler) this.field_36595).getVerticalCellCount() + 1; i++) {
            x[i] = ((IChunkNoiseSampler) this.field_36595).getStartBlockX() + ((IChunkNoiseSampler) this.field_36595).getCellBlockX();
            y[i] = (i + ((IChunkNoiseSampler) this.field_36595).getMinimumCellY()) * ((IChunkNoiseSampler) this.field_36595).getVerticalCellBlockCount();
            z[i] = ((IChunkNoiseSampler) this.field_36595).getStartBlockZ() + ((IChunkNoiseSampler) this.field_36595).getCellBlockZ();
        }
    }
}
