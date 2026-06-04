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
