package com.ishland.c2me.tests.testmod.mixin.fix.logspam;

import net.minecraft.world.gen.heightprovider.UniformHeightProvider;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(UniformHeightProvider.class)
public class MixinUniformHeightProvider {

    @Redirect(method = "get", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"))
    private void redirectLog(Logger instance, String s, Object o) {
        // no-op
    }

}
