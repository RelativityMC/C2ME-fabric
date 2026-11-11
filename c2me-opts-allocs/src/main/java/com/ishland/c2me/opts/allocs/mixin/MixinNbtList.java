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

package com.ishland.c2me.opts.allocs.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(NbtList.class)
public abstract class MixinNbtList {

    @Shadow @Final private List<NbtElement> value;

    /**
     * @author ishland
     * @reason copy using fastutil list
     */
    @Overwrite
    public NbtList copy() {
        List<NbtElement> list = new ObjectArrayList<>(this.value.size());

        for (NbtElement nbtElement : this.value) {
            list.add(nbtElement.copy());
        }

        return new NbtList(list);
    }

    @ModifyArg(method = "<init>()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtList;<init>(Ljava/util/List;)V"), index = 0)
    private static List<NbtElement> modifyList(List<NbtElement> list) {
        return new ObjectArrayList<>();
    }

//    @Redirect(method = "<init>()V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;", remap = false))
//    private static <E> ArrayList<E> redirectNewArrayList() {
//        return null; // avoid double list creation
//    }

}
