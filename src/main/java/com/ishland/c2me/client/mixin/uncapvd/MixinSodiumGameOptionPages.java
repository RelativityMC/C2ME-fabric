package com.ishland.c2me.client.mixin.uncapvd;

import com.ishland.c2me.common.config.C2MEConfig;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages")
public class MixinSodiumGameOptionPages {

    @Dynamic
    @ModifyConstant(method = "*", constant = @Constant(intValue = 32), remap = false)
    private static int modifyMaxViewDistance(int value) {
        return C2MEConfig.clientSideConfig.maxViewDistance;
    }

}
