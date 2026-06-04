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

import com.ishland.c2me.rewrites.chunk_serializer.common.NbtWriter;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TestWeaklyTypedNbt {

    @Test
    public void test() {
        Random rand = new Random();

        NbtCompound root = new NbtCompound();
        NbtList theList = new NbtList();
        theList.add(NbtByte.of((byte) rand.nextInt(Byte.MAX_VALUE)));
        theList.add(NbtShort.of((short) rand.nextInt(Short.MAX_VALUE)));
        theList.add(NbtInt.of(rand.nextInt()));
        theList.add(NbtLong.of(rand.nextLong()));
        theList.add(NbtFloat.of(rand.nextFloat()));
        theList.add(NbtDouble.of(rand.nextDouble()));
        byte[] bytes = new byte[4096];
        rand.nextBytes(bytes);
        theList.add(new NbtByteArray(bytes));
        theList.add(NbtString.of("Something"));
        theList.add(new NbtList(List.of(NbtString.of("a"), NbtString.of("bbbb"))));
        theList.add(new NbtCompound(Map.of()));
        theList.add(new NbtCompound(Map.of("a", NbtString.of("b"))));
        theList.add(new NbtCompound(Map.of("", NbtString.of("c"))));
        int[] ints = new int[1024];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = rand.nextInt();
        }
        theList.add(new NbtIntArray(ints));
        long[] longs = new long[1024];
        for (int i = 0; i < longs.length; i++) {
            longs[i] = rand.nextLong();
        }
        theList.add(new NbtLongArray(longs));
        root.put("smth", theList);

        NbtWriter writer = new NbtWriter();
        writer.start(NbtElement.COMPOUND_TYPE);
        writer.getVisitor().visitCompound(root);
        byte[] serialized = writer.toByteArray();
        writer.release();

        NbtCompound deserialized;
        try (final DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized))) {
            deserialized = NbtIo.readCompound(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(root, deserialized);
    }

}
