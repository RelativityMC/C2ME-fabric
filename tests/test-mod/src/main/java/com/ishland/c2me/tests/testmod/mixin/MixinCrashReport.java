package com.ishland.c2me.tests.testmod.mixin;

import net.minecraft.util.crash.CrashReport;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport {

    @Shadow public abstract String asString();

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "writeToFile", at = @At("HEAD"))
    private void onWriteToFile(File file, CallbackInfoReturnable<Boolean> cir) {
        for (String s : asString().split("\n")) {
            LOGGER.error(s);
        }
        Runtime.getRuntime().halt(-1);
    }

}
