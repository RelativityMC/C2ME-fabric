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

package com.ishland.c2me.opts.worldgen.vanilla.mixin.tlcache;

import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public class MixinBlock {

    @Unique
    private static final ThreadLocal<Object2BooleanLinkedOpenHashMap<VoxelShape>> c2me$full_cube_shape_cache_tl =
            ThreadLocal.withInitial(() -> new Object2BooleanLinkedOpenHashMap<>(256, 0.25F) {
                @Override
                protected void rehash(int newN) {
                    if (newN > n) {
                        super.rehash(newN);
                    }
                }
            });

    /**
     * @author ishland
     * @reason use ThreadLocal cache
     */
    @Overwrite
    public static boolean isShapeFullCube(VoxelShape shape) {
        Object2BooleanLinkedOpenHashMap<VoxelShape> map = c2me$full_cube_shape_cache_tl.get();
        if (map.containsKey(shape)) {
            return map.getAndMoveToFirst(shape);
        } else {
            boolean uncached = !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), shape, BooleanBiFunction.NOT_SAME);
            if (map.size() >= 256) {
                map.removeLastBoolean();
            }
            map.putAndMoveToFirst(shape, uncached);
            return uncached;
        }
    }

}
