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

package com.ishland.c2me.fixes.worldgen.threading_issues.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Stream;

public class ASMTransformerMakeVolatile {

    static final Logger LOGGER = LoggerFactory.getLogger("C2ME (c2me-fixes-worldgen-threading-issues) ASM Transformer");

    public static void transform(ClassNode classNode) {
        classNode.fields.stream()
                .filter(fieldNode ->
                        Stream.concat(Stream.ofNullable(fieldNode.visibleAnnotations), Stream.ofNullable(fieldNode.invisibleAnnotations))
                                .flatMap(Collection::stream)
                                .anyMatch(annotationNode -> Type.getDescriptor(MakeVolatile.class).equals(annotationNode.desc))
                )
                .forEach(fieldNode -> {
                    LOGGER.debug("Making field L{};{}:{} volatile", classNode.name, fieldNode.name, fieldNode.desc);
                    fieldNode.access |= Opcodes.ACC_VOLATILE;
                });
    }

    private record KeyValue<K, V>(K key, V value) {
    }

}
