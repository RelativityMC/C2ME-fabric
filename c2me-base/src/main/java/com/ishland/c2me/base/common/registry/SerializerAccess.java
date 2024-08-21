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
