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
