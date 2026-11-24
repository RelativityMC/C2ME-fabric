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

package com.ishland.c2me.opts.worldgen.vanilla.mixin.biome_search_tree;

import com.ishland.c2me.opts.worldgen.vanilla.common.biome_search_tree.FlatBiomeSearchTree;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MultiNoiseUtil.Entries.class)
public class MixinMultiNoiseUtilEntries<T> {

    @Shadow
    @Final
    private MultiNoiseUtil.SearchTree<T> tree;

    @Unique
    private @Nullable FlatBiomeSearchTree<T> c2me$flatSearchTree;

    @Inject(method = "<init>(Ljava/util/List;I)V", at = @At("RETURN"))
    private void c2me$initFlatSearchTree(List<Pair<MultiNoiseUtil.NoiseHypercube, T>> entries, int maxChildren, CallbackInfo ci) {
        if ((Object) this.getClass() == MultiNoiseUtil.Entries.class) {
            this.c2me$flatSearchTree = FlatBiomeSearchTree.fromSearchTree(this.tree);
        }
    }

    @WrapMethod(method = "getValue(Lnet/minecraft/world/biome/source/util/MultiNoiseUtil$NoiseValuePoint;)Ljava/lang/Object;")
    private T c2me$getValue(MultiNoiseUtil.NoiseValuePoint point, Operation<T> original) {
        FlatBiomeSearchTree<T> flatTree = this.c2me$flatSearchTree;
        if (flatTree != null) {
            return flatTree.search(point);
        }
        return original.call(point);
    }

}
