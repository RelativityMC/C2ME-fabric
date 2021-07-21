package com.ishland.c2me;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class C2MEMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("C2ME");

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            MixinEnvironment.getCurrentEnvironment().audit();
        }
    }
}
