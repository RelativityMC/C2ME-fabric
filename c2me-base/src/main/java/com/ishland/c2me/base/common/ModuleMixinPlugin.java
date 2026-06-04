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

package com.ishland.c2me.base.common;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ModuleMixinPlugin implements IMixinConfigPlugin {

    static final Logger LOGGER = LoggerFactory.getLogger("C2ME Modules Manager");

    protected boolean isEnabled = true;

    @Override
    public void onLoad(String mixinPackage) {
        MixinExtrasBootstrap.init();
        LOGGER.info("Initializing {}", mixinPackage);
        final String[] split = mixinPackage.split("\\.");
        final String targetClass = String.join(".", Arrays.copyOf(split, split.length - 1)) + ".ModuleEntryPoint";
        try {
            final Class<?> entryPoint = Class.forName(targetClass);
            try {
                final Field enabled = entryPoint.getDeclaredField("enabled");
                enabled.setAccessible(true);
                isEnabled = (boolean) enabled.get(null);
            } catch (Throwable t) {
                LOGGER.warn("Unable to detect enabled state for module entrypoint: {}", targetClass, t);
            }
        } catch (Throwable t) {
            LOGGER.warn("Error loading module entrypoint: {}", targetClass, t);
        }
        if (!isEnabled) {
            LOGGER.info("Disabling {}", mixinPackage);
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return isEnabled;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
