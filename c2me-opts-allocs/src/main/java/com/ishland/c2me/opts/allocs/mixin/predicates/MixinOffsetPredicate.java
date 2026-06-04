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

package com.ishland.c2me.opts.allocs.mixin.predicates;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.OffsetPredicate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OffsetPredicate.class)
public abstract class MixinOffsetPredicate {

    @Shadow protected abstract boolean test(BlockState state);

    @Shadow @Final protected Vec3i offset;

    /**
     * @author ishland
     * @reason reduce allocs
     */
    @Overwrite
    public final boolean test(StructureWorldAccess structureWorldAccess, BlockPos blockPos) {
        if (blockPos instanceof BlockPos.Mutable mutable) {
            int savedX = mutable.getX();
            int savedY = mutable.getY();
            int savedZ = mutable.getZ();
            boolean res = this.test(structureWorldAccess.getBlockState(mutable.set(savedX + this.offset.getX(), savedY + this.offset.getY(), savedZ + this.offset.getZ())));
            mutable.set(savedX, savedY, savedZ);
            return res;
        } else {
            return this.test(structureWorldAccess.getBlockState(blockPos.add(this.offset)));
        }
    }

}
