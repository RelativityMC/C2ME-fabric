package com.ishland.c2me.client.uncapvd.mixin;

import com.ishland.c2me.client.uncapvd.common.Config;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(targets = "net.vulkanmod.config.option.Options")
public class MixinVKModOptions {

    @Dynamic
    @ModifyConstant(method = "getGraphicsOpts", constant = @Constant(intValue = 32, ordinal = 0), remap = false, require = 0)
    private static int modifyMaxViewDistance(int value) {
        return Config.maxViewDistance;
    }

}
