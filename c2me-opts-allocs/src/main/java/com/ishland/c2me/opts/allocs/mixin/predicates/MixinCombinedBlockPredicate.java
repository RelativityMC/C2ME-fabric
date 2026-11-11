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

import com.ishland.c2me.opts.allocs.common.ducks.CombinedBlockPredicateExtension;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.CombinedBlockPredicate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(CombinedBlockPredicate.class)
public class MixinCombinedBlockPredicate implements CombinedBlockPredicateExtension {

    @Shadow @Final protected List<BlockPredicate> predicates;

    @Unique
    private BlockPredicate[] c2me$predicatesArray;

    @Override
    public BlockPredicate[] c2me$getPredicatesArray() {
        BlockPredicate[] predicateArray = this.c2me$predicatesArray;
        if (predicateArray == null) {
            this.c2me$predicatesArray = predicateArray = this.predicates.toArray(BlockPredicate[]::new);
        }
        return predicateArray;
    }

}
