package com.ishland.c2me.client.uncapvd.mixin;

import net.minecraft.client.option.DoubleOption;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameOptions.class)
public class MixinGameOptions {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/DoubleOption;setMax(F)V"))
    private void redirectSetMaxVD(DoubleOption doubleOption, float max) {
        if (doubleOption == Option.RENDER_DISTANCE) return;
        doubleOption.setMax(max);
    }

}
