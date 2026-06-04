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
