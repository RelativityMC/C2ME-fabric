package com.ishland.c2me.opts.scheduling.mixin.shutdown;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @Shadow(aliases = "field_47139") private long timeReference;

    @Inject(method = "shutdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;runTasksTillTickEnd()V", shift = At.Shift.BEFORE))
    private void shutdownBeforeRunTasks(CallbackInfo ci) {
        this.timeReference = Util.getMeasuringTimeMs() + 100L;
    }

}
