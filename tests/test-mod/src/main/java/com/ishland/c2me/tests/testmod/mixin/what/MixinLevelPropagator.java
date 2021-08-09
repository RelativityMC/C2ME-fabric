package com.ishland.c2me.tests.testmod.mixin.what;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.minecraft.world.chunk.light.LevelPropagator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LevelPropagator.class)
public abstract class MixinLevelPropagator {

    @Shadow @Final private int levelCount;

    @Shadow private int minPendingLevel;

    @Shadow protected abstract void increaseMinPendingLevel(int maxLevel);

    @Redirect(method = "applyPendingUpdates", at = @At(value = "FIELD", target = "Lnet/minecraft/world/chunk/light/LevelPropagator;pendingIdUpdatesByLevel:[Lit/unimi/dsi/fastutil/longs/LongLinkedOpenHashSet;", args = "array=get"))
    private LongLinkedOpenHashSet redirectPendingIdUpdatesByLevelAccess(LongLinkedOpenHashSet[] array, int index) {
        LongLinkedOpenHashSet longs = array[index];
        while (this.minPendingLevel < this.levelCount && longs.isEmpty()) {
            System.err.println("Whats going on with LevelPropagator? Recovering state...");
            this.increaseMinPendingLevel(this.levelCount);
            if (this.minPendingLevel < this.levelCount) longs = array[this.minPendingLevel];
            else longs = null;
        }
        return longs;
    }

    @Inject(method = "applyPendingUpdates", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/LongLinkedOpenHashSet;removeFirstLong()J", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void beforeRemoveFirstLong(int maxSteps, CallbackInfoReturnable<Integer> cir, LongLinkedOpenHashSet longs) {
        if (longs == null) {
            System.err.println("Whats going on with LevelPropagator? Recovering state...");
            cir.setReturnValue(maxSteps);
        }
    }

}
