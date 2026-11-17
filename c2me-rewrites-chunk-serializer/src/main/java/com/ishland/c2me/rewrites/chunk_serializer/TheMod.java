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

package com.ishland.c2me.rewrites.chunk_serializer;

import com.ibm.asyncutil.util.Either;
import com.ishland.c2me.base.common.registry.SerializerAccess;
import com.ishland.c2me.rewrites.chunk_serializer.common.ChunkDataSerializer;
import com.ishland.c2me.rewrites.chunk_serializer.common.NbtWriter;
import com.ishland.c2me.rewrites.chunk_serializer.common.utils.ValidationUtils;
import net.minecraft.nbt.NbtElement;

public class TheMod implements net.fabricmc.api.ModInitializer {
    @Override
    public void onInitialize() {
        if (ModuleEntryPoint.enabled) {
            SerializerAccess.registerSerializer(serializable -> {
                NbtWriter nbtWriter = new NbtWriter();
                nbtWriter.start(NbtElement.COMPOUND_TYPE);
                ChunkDataSerializer.write(serializable, nbtWriter);
                nbtWriter.finishCompound();
                final byte[] data = nbtWriter.toByteArray();
                nbtWriter.release();
                ValidationUtils.validateNbt(data);
                return Either.right(data);
            });
        }
    }
}
