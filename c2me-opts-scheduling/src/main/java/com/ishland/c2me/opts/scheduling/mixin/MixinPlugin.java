package com.ishland.c2me.opts.scheduling.mixin;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import com.ishland.c2me.opts.scheduling.common.Config;

public class MixinPlugin extends ModuleMixinPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (super.shouldApplyMixin(targetClassName, mixinClassName)) {
            if (mixinClassName.startsWith("com.ishland.c2me.opts.scheduling.mixin.idle_tasks.autosave.disable_vanilla_mid_tick_autosave."))
                return Config.autoSaveMode != Config.AutoSaveMode.VANILLA;
            if (mixinClassName.startsWith("com.ishland.c2me.opts.scheduling.mixin.idle_tasks.autosave.enhanced_autosave."))
                return Config.autoSaveMode == Config.AutoSaveMode.ENHANCED;
            if (mixinClassName.startsWith("com.ishland.c2me.opts.scheduling.mixin.mid_tick_chunk_tasks."))
                return Config.midTickChunkTasksInterval > 0;
            return true;
        } else {
            return false;
        }
    }
}
