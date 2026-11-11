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

package com.ishland.c2me.rewrites.chunksystem;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import com.ishland.c2me.rewrites.chunksystem.common.Config;

import java.lang.reflect.Field;

public class MixinPlugin extends ModuleMixinPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!super.shouldApplyMixin(targetClassName, mixinClassName))
            return false;

        boolean gcFreeChunkSerializerDetected = tryDetectGcFreeSerializer();

        if (mixinClassName.startsWith("com.ishland.c2me.rewrites.chunksystem.mixin.serialization_sync."))
            return !gcFreeChunkSerializerDetected;

        if (mixinClassName.startsWith("com.ishland.c2me.rewrites.chunksystem.mixin.fluid_postprocessing"))
            return Config.fluidPostProcessingToScheduledTick;

        return true;
    }

    private static boolean tryDetectGcFreeSerializer() {
        try {
            Class<?> entryPoint = Class.forName("com.ishland.c2me.rewrites.chunk_serializer.ModuleEntryPoint");
            Field enabled = entryPoint.getField("enabled");
            return (boolean) enabled.get(null);
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
}
