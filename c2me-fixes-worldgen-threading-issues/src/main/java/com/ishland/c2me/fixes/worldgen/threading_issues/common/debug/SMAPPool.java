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

package com.ishland.c2me.fixes.worldgen.threading_issues.common.debug;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.MixinService;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SMAPPool {

    private static final ConcurrentHashMap<String, Optional<String>> CACHE = new ConcurrentHashMap<>();

    public static void put(String className, ClassNode node) {
        if (className == null || node == null) {
            return;
        }
        className = className.replace('/', '.');
        CACHE.put(className, Optional.ofNullable(node.sourceDebug));
    }

    public static String getSourceDebugInfo(String className) {
        Optional<String> cached = CACHE.get(className);
        if (cached != null) {
            return cached.orElse(null);
        }

        try {
            IClassBytecodeProvider provider = MixinService.getService().getBytecodeProvider();
            ClassNode classNode = provider.getClassNode(className.replace('.', '/'));

            if (classNode != null) {
                put(className, classNode);
                return classNode.sourceDebug;
            }
        } catch (Exception e) {
            // ignore
        }

        CACHE.put(className, Optional.empty());
        return null;
    }

    private SMAPPool() {
    }

}
