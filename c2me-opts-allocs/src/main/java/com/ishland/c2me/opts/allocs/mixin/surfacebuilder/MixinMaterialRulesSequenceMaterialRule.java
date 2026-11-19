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

import com.google.common.collect.ImmutableList;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MaterialRules.SequenceMaterialRule.class)
public class MixinMaterialRulesSequenceMaterialRule {

    private static final MaterialRules.BlockStateRule EMPTY = new MaterialRules.SequenceBlockStateRule(List.of());

    @Shadow
    @Final
    private List<MaterialRules.MaterialRule> sequence;

    @Unique
    private MaterialRules.MaterialRule[] sequenceArray;

    @Unique
    private boolean isSingleOrNoElement;

    @Unique
    private MaterialRules.MaterialRule firstElement;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.sequenceArray = this.sequence.toArray(MaterialRules.MaterialRule[]::new);
        this.isSingleOrNoElement = this.sequenceArray.length <= 1;
        this.firstElement = this.sequenceArray.length == 0 ? null : this.sequenceArray[0];
    }

    /**
     * @author ishland
     * @reason optimize lookup
     */
    @Overwrite
    public MaterialRules.BlockStateRule apply(MaterialRules.MaterialRuleContext materialRuleContext) {
        if (this.isSingleOrNoElement) {
            return this.firstElement != null ? this.firstElement.apply(materialRuleContext) : EMPTY;
        } else {
            @SuppressWarnings("UnstableApiUsage")
            ImmutableList.Builder<MaterialRules.BlockStateRule> builder = ImmutableList.builderWithExpectedSize(this.sequenceArray.length);

            for (MaterialRules.MaterialRule materialRule : this.sequenceArray) {
                builder.add(materialRule.apply(materialRuleContext));
            }

            return new MaterialRules.SequenceBlockStateRule(builder.build());
        }
    }

}
