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
