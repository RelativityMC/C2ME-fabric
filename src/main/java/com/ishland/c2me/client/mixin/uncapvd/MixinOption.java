package com.ishland.c2me.client.mixin.uncapvd;

import com.ishland.c2me.common.config.C2MEConfig;
import net.minecraft.client.option.Option;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Option.class)
public class MixinOption {

    @Dynamic("Mixin AP and MinecraftDev cannot see this")
    @ModifyConstant(method = "<clinit>", constant = @Constant(doubleValue = 16.0D))
    private static double modifyMaxViewDistance(double value) {
        return C2MEConfig.clientSideConfig.maxViewDistance;
    }

}
