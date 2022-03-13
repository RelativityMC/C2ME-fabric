package com.ishland.c2me.opts.chunkio;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import com.ishland.c2me.opts.chunkio.common.Config;

public class MixinPlugin extends ModuleMixinPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!super.shouldApplyMixin(targetClassName, mixinClassName)) return false;

        if (mixinClassName.startsWith("com.ishland.c2me.opts.chunkio.mixin.compression.modify_default_chunk_compression"))
            return Config.chunkStreamVersion != -1;

        return true;
    }
}
