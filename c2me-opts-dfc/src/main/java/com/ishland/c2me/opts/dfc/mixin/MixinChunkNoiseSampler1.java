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

package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.base.mixin.access.IChunkNoiseSampler;
import com.ishland.c2me.opts.dfc.common.ducks.IArrayCacheCapable;
import com.ishland.c2me.opts.dfc.common.ducks.ICoordinatesFilling;
import com.ishland.c2me.opts.dfc.common.util.ArrayCache;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/gen/chunk/ChunkNoiseSampler$1")
public class MixinChunkNoiseSampler1 implements IArrayCacheCapable, ICoordinatesFilling {
    @Shadow
    @Final
    ChunkNoiseSampler field_36595;

    @Override
    public ArrayCache c2me$getArrayCache() {
        return ((IArrayCacheCapable) this.field_36595).c2me$getArrayCache();
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
