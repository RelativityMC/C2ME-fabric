package com.ishland.c2me.tests.worlddiff.mixin;

import net.minecraft.world.updater.WorldUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldUpdater.class)
public class MixinWorldUpdater {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V"))
    private void redirectThreadStart(Thread thread) {
    }

}
