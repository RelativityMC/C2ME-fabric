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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.Map;

@Mixin(targets = "net.minecraft.nbt.NbtCompound$1")
public abstract class MixinNbtCompound1 implements NbtType<NbtCompound> {

    @SuppressWarnings("UnnecessaryQualifiedMemberReference")
    @ModifyVariable(method = "readCompound(Ljava/io/DataInput;Lnet/minecraft/nbt/NbtSizeTracker;)Lnet/minecraft/nbt/NbtCompound;", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
    private static Map<String, NbtElement> modifyMap(Map<String, NbtElement> map) {
        return new Object2ObjectOpenHashMap<>();
    }

    @SuppressWarnings("UnnecessaryQualifiedMemberReference")
    @Redirect(method = "readCompound(Ljava/io/DataInput;Lnet/minecraft/nbt/NbtSizeTracker;)Lnet/minecraft/nbt/NbtCompound;", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
    private static  <K, V> HashMap<K, V> redirectNewHashMap() {
        return null; // avoid double map creation
    }

}
