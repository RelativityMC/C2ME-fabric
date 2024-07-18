package com.ishland.c2me.rewrites.chunksystem;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import com.ishland.c2me.rewrites.chunksystem.common.Config;

public class MixinPlugin extends ModuleMixinPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!super.shouldApplyMixin(targetClassName, mixinClassName))
            return false;

        if (mixinClassName.startsWith("com.ishland.c2me.rewrites.chunksystem.mixin.async_chunkio."))
            return Config.asyncSerialization;

        return true;
    }
}
