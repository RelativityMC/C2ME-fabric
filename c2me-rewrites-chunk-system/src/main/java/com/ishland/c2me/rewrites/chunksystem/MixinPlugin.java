/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
