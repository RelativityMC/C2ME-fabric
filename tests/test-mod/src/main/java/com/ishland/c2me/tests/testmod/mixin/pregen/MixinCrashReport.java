package com.ishland.c2me.tests.testmod.mixin.pregen;

import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.ReportType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.List;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport {

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract String asString(ReportType type, List<String> extraInfo);

    @Inject(method = "writeToFile(Ljava/nio/file/Path;Lnet/minecraft/util/crash/ReportType;Ljava/util/List;)Z", at = @At("HEAD"))
    private void onWriteToFile(Path path, ReportType type, List<String> extraInfo, CallbackInfoReturnable<Boolean> cir) {
        for (String s : asString(ReportType.MINECRAFT_CRASH_REPORT, extraInfo).split("\n")) {
            LOGGER.error(s);
        }
        Runtime.getRuntime().halt(-1);
    }

}
