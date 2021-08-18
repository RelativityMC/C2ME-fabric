package com.ishland.c2me.tests.testmod.mixin;

import net.minecraft.server.ServerTask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerTask.class)
public class MixinServerTask {

    @Mutable
    @Shadow @Final private int creationTicks;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(int creationTicks, Runnable runnable, CallbackInfo ci) {
        this.creationTicks = Integer.MIN_VALUE;
    }

}
