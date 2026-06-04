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
