package com.ishland.c2me.opts.worldgen.vanilla;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import com.ishland.c2me.opts.worldgen.vanilla.common.Config;

public class MixinPlugin extends ModuleMixinPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!super.shouldApplyMixin(targetClassName, mixinClassName)) return false;

        if (mixinClassName.startsWith("com.ishland.c2me.opts.worldgen.vanilla.mixin.aquifer."))
            return Config.optimizeAquifer;

        if (mixinClassName.startsWith("com.ishland.c2me.opts.worldgen.vanilla.mixin.the_end_biome_cache."))
            return Config.useEndBiomeCache;

        return true;
    }
}
