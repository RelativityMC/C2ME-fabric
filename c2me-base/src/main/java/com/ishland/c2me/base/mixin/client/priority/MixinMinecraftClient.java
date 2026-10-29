package com.ishland.c2me.base.mixin.client.priority;

import com.ishland.c2me.base.common.threadpriority.bindings.LinuxLibc;
import com.ishland.c2me.base.common.threadpriority.bindings.WindowsKernel32Extras;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import oshi.SystemInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "run()V", at = @At("HEAD"))
    private void onRun(CallbackInfo ci) {
        switch (SystemInfo.getCurrentPlatform()) {
            case LINUX, ANDROID -> {
                LinuxLibc.setCurrentThreadPreferMaximumPerformance();
            }
            case WINDOWS -> {
                WindowsKernel32Extras.disableCurrentThreadPowerThrottling();
            }
        }
    }

}
