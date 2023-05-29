package com.ishland.c2me.rewrites.chunk_serializer;

import com.ibm.asyncutil.util.Either;
import com.ishland.c2me.base.common.registry.SerializerAccess;
import com.ishland.c2me.rewrites.chunk_serializer.common.ChunkDataSerializer;
import com.ishland.c2me.rewrites.chunk_serializer.common.NbtWriter;
import net.minecraft.nbt.NbtElement;

public class TheMod implements net.fabricmc.api.ModInitializer {
    @Override
    public void onInitialize() {
        if (ModuleEntryPoint.enabled) {
            SerializerAccess.registerSerializer((world, chunk) -> {
                NbtWriter nbtWriter = new NbtWriter();
                nbtWriter.start(NbtElement.COMPOUND_TYPE);
                ChunkDataSerializer.write(world, chunk, nbtWriter);
                nbtWriter.finishCompound();
                final byte[] data = nbtWriter.toByteArray();
                nbtWriter.release();
                return Either.right(data);
            });
        }
    }
}
