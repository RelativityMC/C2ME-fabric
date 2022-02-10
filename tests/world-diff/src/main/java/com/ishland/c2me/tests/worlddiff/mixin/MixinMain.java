package com.ishland.c2me.tests.worlddiff.mixin;

import net.minecraft.server.Main;
import net.minecraft.server.dedicated.EulaReader;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MixinMain {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/EulaReader;isEulaAgreedTo()Z"))
    private static boolean redirectEULA(EulaReader eulaReader) {
        LOGGER.info("Automatically agreed to EULA. If you don't, please stop using this test suite.");
        return true;
    }

    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;startTimerHack()V", shift = At.Shift.AFTER), cancellable = true)
    private static void afterInit(CallbackInfo ci) {
        ci.cancel();
        com.ishland.c2me.tests.worlddiff.Main.main();
    }

}
