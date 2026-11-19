/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
