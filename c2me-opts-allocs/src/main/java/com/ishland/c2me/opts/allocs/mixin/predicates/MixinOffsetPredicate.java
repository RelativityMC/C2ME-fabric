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

package com.ishland.c2me.opts.allocs.mixin.predicates;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
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
    public final boolean test(WorldAccess worldAccess, BlockPos blockPos) {
        if (blockPos instanceof BlockPos.Mutable mutable) {
            int savedX = mutable.getX();
            int savedY = mutable.getY();
            int savedZ = mutable.getZ();
            boolean res = this.test(worldAccess.getBlockState(mutable.set(savedX + this.offset.getX(), savedY + this.offset.getY(), savedZ + this.offset.getZ())));
            mutable.set(savedX, savedY, savedZ);
            return res;
        } else {
            return this.test(worldAccess.getBlockState(blockPos.add(this.offset)));
        }
    }

}
