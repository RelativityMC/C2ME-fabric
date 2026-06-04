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

package com.ishland.c2me.base.common.registry;

import com.ibm.asyncutil.util.Either;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.SerializedChunk;

/**
 * Only used for c2me-threading-chunkio
 */
public class SerializerAccess {

    private final static Serializer VANILLA = serializable -> Either.left(serializable.serialize());

    private static Serializer activeSerializer = null;

    public static void registerSerializer(Serializer serializer) {
        if (serializer == null) {
            throw new NullPointerException("serializer");
        } else if (activeSerializer != null) {
            throw new IllegalStateException("Serializer already registered");
        } else {
            activeSerializer = serializer;
        }
    }

    public static Serializer getSerializer() {
        return activeSerializer == null ? VANILLA : activeSerializer;
    }

    public interface Serializer {

        com.ibm.asyncutil.util.Either<NbtCompound, byte[]> serialize(SerializedChunk serializable);

    }

}
