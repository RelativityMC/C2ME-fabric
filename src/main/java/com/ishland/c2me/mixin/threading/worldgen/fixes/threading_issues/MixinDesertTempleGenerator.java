package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

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
        return this.hasPlacedChestAtomic.get(index);
    }

}
