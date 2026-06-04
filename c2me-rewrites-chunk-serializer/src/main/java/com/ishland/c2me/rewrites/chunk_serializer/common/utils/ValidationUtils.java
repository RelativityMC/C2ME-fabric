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

package com.ishland.c2me.rewrites.chunk_serializer.common.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class ValidationUtils {

    private static final boolean VALIDATION_ENABLED = Boolean.getBoolean("c2me.gcFreeChunkSerializer.validateNbt");

    static {
        if (VALIDATION_ENABLED) {
            System.out.println("NBT validation for gcFreeChunkSerializer is enabled. This *will* impact performance.");
        }
    }

    public static void validateNbt(byte[] bytes) {
        if (!VALIDATION_ENABLED) {
            return;
        }
        try (final var in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            NbtCompound read = NbtIo.readCompound(in);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
