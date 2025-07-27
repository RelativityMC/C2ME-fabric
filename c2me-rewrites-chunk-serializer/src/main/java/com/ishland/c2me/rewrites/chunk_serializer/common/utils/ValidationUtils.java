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
