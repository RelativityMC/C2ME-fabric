package com.ishland.c2me.base.mixin.util.log4j2shutdownhookisnomore;

import net.minecraft.server.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MixinMain {

    @Inject(method = "main", at = @At("HEAD"), remap = false)
    private static void preMain(CallbackInfo info) {
        try {
            ((DefaultShutdownCallbackRegistry) ((Log4jContextFactory) LogManager.getFactory()).getShutdownCallbackRegistry()).stop();
        } catch (Throwable t) {
            System.err.println("Unable to remove log4j2 shutdown hook");
            t.printStackTrace();
        }
    }

}
