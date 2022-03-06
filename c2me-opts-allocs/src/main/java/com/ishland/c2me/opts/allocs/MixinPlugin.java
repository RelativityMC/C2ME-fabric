package com.ishland.c2me.opts.allocs;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import net.fabricmc.loader.api.FabricLoader;

public class MixinPlugin extends ModuleMixinPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!super.shouldApplyMixin(targetClassName, mixinClassName)) return false;

        if (mixinClassName.equals("com.ishland.c2me.opts.allocs.mixin.MixinNbtCompound") ||
                mixinClassName.equals("com.ishland.c2me.opts.allocs.mixin.MixinNbtCompound1"))
            return !FabricLoader.getInstance().isModLoaded("lithium");

        return true;
    }
}
