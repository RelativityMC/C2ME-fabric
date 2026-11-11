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

package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import net.minecraft.structure.DesertTempleGenerator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(DesertTempleGenerator.class)
public abstract class MixinDesertTempleGenerator {

    private final AtomicReferenceArray<Boolean> hasPlacedChestAtomic = new AtomicReferenceArray<>(new Boolean[4]);

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        for (int i = 0; i < this.hasPlacedChestAtomic.length(); i ++) {
            if (this.hasPlacedChestAtomic.get(i) == null) {
                this.hasPlacedChestAtomic.set(i, false);
            }
        }
    }

    @Dynamic
    @SuppressWarnings({"InvalidInjectorMethodSignature", "RedundantSuppression"})
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/DesertTempleGenerator;hasPlacedChest:[Z", opcode = Opcodes.GETFIELD, args = "array=set"))
    private void redirectSetHasPlacedChest(boolean[] array, int index, boolean value) {
        this.hasPlacedChestAtomic.compareAndSet(index, false, value);
    }

    @Dynamic
    @SuppressWarnings({"InvalidInjectorMethodSignature", "RedundantSuppression"})
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/DesertTempleGenerator;hasPlacedChest:[Z", opcode = Opcodes.GETFIELD, args = "array=get"))
    private boolean redirectGetHasPlacedChest(boolean[] array, int index) {
        final Boolean aBoolean = this.hasPlacedChestAtomic.get(index);
        return aBoolean != null ? aBoolean : false;
    }

}
