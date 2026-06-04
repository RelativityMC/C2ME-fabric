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

package com.ishland.c2me.fixes.worldgen.threading_issues;

import com.ishland.c2me.base.common.ModuleMixinPlugin;
import com.ishland.c2me.fixes.worldgen.threading_issues.asm.ASMTransformerMakeVolatile;
import com.ishland.c2me.fixes.worldgen.threading_issues.common.debug.SMAPPool;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

public class MixinPlugin extends ModuleMixinPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        super.onLoad(mixinPackage);
        if (MixinEnvironment.getCurrentEnvironment().getActiveTransformer() instanceof IMixinTransformer transformer &&
            transformer.getExtensions() instanceof Extensions extensions) {
            extensions.add(new IExtension() {
                @Override
                public boolean checkActive(MixinEnvironment environment) {
                    return true;
                }

                @Override
                public void preApply(ITargetClassContext context) {

                }

                @Override
                public void postApply(ITargetClassContext context) {

                }

                @Override
                public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
                    SMAPPool.put(name, classNode);
                }
            });
        } else {
            System.err.println("Failed to initialize SMAP parser for safe world random access, mod information for mixin injected methods will not be available");
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        ASMTransformerMakeVolatile.transform(targetClass);
    }
}
