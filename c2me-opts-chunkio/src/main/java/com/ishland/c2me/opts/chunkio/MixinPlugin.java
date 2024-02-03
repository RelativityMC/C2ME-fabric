package com.ishland.c2me.opts.chunkio;

import com.ishland.c2me.base.common.ModuleMixinPlugin;

public class MixinPlugin extends ModuleMixinPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!super.shouldApplyMixin(targetClassName, mixinClassName)) return false;

        return true;
    }
}
