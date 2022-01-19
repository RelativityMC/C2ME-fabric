package com.ishland.c2me.client.mixin.uncapvd;

import com.ishland.c2me.common.config.C2MEConfig;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.client.option.Option;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Option.class)
public class MixinOption {

    @Shadow @Final public static DoubleOption RENDER_DISTANCE;

    private static final int C2MERenderDistance = C2MEConfig.clientSideConfig.modifyMaxVDConfig.maxViewDistance;

    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void modifyMaxViewDistance(CallbackInfo ci) {
        if (RENDER_DISTANCE.getMax() < C2MERenderDistance) {
            RENDER_DISTANCE.setMax(C2MERenderDistance);
        }
    }
}
