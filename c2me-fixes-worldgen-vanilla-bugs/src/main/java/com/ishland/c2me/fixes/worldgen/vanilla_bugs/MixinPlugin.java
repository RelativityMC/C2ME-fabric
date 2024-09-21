package com.ishland.c2me.fixes.worldgen.vanilla_bugs;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import com.ishland.c2me.fixes.worldgen.vanilla_bugs.common.Config;

public class MixinPlugin extends ModuleMixinPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!super.shouldApplyMixin(targetClassName, mixinClassName)) return false;

        if (mixinClassName.startsWith("com.ishland.c2me.fixes.worldgen.vanilla_bugs.mixin.mc_276863."))
            return Config.suppressGhostMushrooms;

        return true;
    }
}
