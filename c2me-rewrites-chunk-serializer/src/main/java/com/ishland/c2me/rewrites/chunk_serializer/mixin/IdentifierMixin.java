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

package com.ishland.c2me.rewrites.chunk_serializer.mixin;

import com.ishland.c2me.rewrites.chunk_serializer.common.NbtWriter;
import com.ishland.c2me.rewrites.chunk_serializer.common.utils.StringBytesConvertible;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// TODO: make config option?
@Mixin(Identifier.class)
public class IdentifierMixin implements StringBytesConvertible {
    @Unique
    private byte[] serializedStringBytes;

    @Override
    public byte[] getStringBytes() {
        return this.serializedStringBytes != null ? this.serializedStringBytes :
                (this.serializedStringBytes = NbtWriter.getAsciiStringBytes(this.toString()));
    }
}
