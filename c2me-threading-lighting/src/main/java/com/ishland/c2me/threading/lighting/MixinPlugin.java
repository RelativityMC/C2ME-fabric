package com.ishland.c2me.threading.lighting;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import net.fabricmc.loader.api.FabricLoader;

public class MixinPlugin extends ModuleMixinPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (super.shouldApplyMixin(targetClassName, mixinClassName)) {
            if (mixinClassName.startsWith("com.ishland.c2me.threading.lighting.mixin.starlight."))
                return FabricLoader.getInstance().isModLoaded("starlight");
            return true;
        } else {
            return false;
        }
    }
}
