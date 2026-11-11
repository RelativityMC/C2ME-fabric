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

import net.minecraft.structure.JungleTempleGenerator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(JungleTempleGenerator.class)
public class MixinJungleTempleGenerator {

    private final AtomicBoolean placedMainChestAtomic = new AtomicBoolean();
    private final AtomicBoolean placedHiddenChestAtomic = new AtomicBoolean();
    private final AtomicBoolean placedTrap1Atomic = new AtomicBoolean();
    private final AtomicBoolean placedTrap2Atomic = new AtomicBoolean();

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/JungleTempleGenerator;placedMainChest:Z", opcode = Opcodes.PUTFIELD))
    private void redirectSetPlacedMainChest(JungleTempleGenerator jungleTempleGenerator, boolean value) {
        this.placedMainChestAtomic.compareAndSet(false, value);
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/JungleTempleGenerator;placedMainChest:Z", opcode = Opcodes.GETFIELD))
    private boolean redirectGetPlacedMainChest(JungleTempleGenerator jungleTempleGenerator) {
        return this.placedMainChestAtomic.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/JungleTempleGenerator;placedHiddenChest:Z", opcode = Opcodes.PUTFIELD))
    private void redirectSetHiddenChest(JungleTempleGenerator jungleTempleGenerator, boolean value) {
        this.placedHiddenChestAtomic.compareAndSet(false, value);
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/JungleTempleGenerator;placedHiddenChest:Z", opcode = Opcodes.GETFIELD))
    private boolean redirectGetHiddenChest(JungleTempleGenerator jungleTempleGenerator) {
        return this.placedHiddenChestAtomic.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/JungleTempleGenerator;placedTrap1:Z", opcode = Opcodes.PUTFIELD))
    private void redirectSetPlacedTrap1(JungleTempleGenerator jungleTempleGenerator, boolean value) {
        this.placedTrap1Atomic.compareAndSet(false, value);
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/JungleTempleGenerator;placedTrap1:Z", opcode = Opcodes.GETFIELD))
    private boolean redirectGetPlacedTrap1(JungleTempleGenerator jungleTempleGenerator) {
        return this.placedTrap1Atomic.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/JungleTempleGenerator;placedTrap2:Z", opcode = Opcodes.PUTFIELD))
    private void redirectSetPlacedTrap2(JungleTempleGenerator jungleTempleGenerator, boolean value) {
        this.placedTrap2Atomic.compareAndSet(false, value);
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/JungleTempleGenerator;placedTrap2:Z", opcode = Opcodes.GETFIELD))
    private boolean redirectGetPlacedTrap2(JungleTempleGenerator jungleTempleGenerator) {
        return this.placedTrap2Atomic.get();
    }

}
