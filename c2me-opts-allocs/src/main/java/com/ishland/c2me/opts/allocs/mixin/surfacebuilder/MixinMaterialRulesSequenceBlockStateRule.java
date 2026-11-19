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

package com.ishland.c2me.opts.allocs.mixin.surfacebuilder;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MaterialRules.SequenceBlockStateRule.class)
public class MixinMaterialRulesSequenceBlockStateRule {

    @Shadow @Final private List<MaterialRules.BlockStateRule> rules;
    @Unique
    private MaterialRules.BlockStateRule[] rulesArray;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.rulesArray = this.rules.toArray(MaterialRules.BlockStateRule[]::new);
    }

    /**
     * @author ishland
     * @reason use array for iteration
     */
    @Overwrite
    public @Nullable BlockState tryApply(int i, int j, int k) {
        // TODO [VanillaCopy]
        for(MaterialRules.BlockStateRule blockStateRule : this.rulesArray) {
            BlockState blockState = blockStateRule.tryApply(i, j, k);
            if (blockState != null) {
                return blockState;
            }
        }

        return null;
    }

}
