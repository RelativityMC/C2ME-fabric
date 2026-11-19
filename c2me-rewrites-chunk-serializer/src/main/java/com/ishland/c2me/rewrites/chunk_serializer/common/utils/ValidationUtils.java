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
