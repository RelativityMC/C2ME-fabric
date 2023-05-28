package com.ishland.c2me.rewrites.chunk_serializer.mixin;

import com.ishland.c2me.rewrites.chunk_serializer.common.ChunkStatusAccessor;
import com.ishland.c2me.rewrites.chunk_serializer.common.NbtWriter;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChunkStatus.class)
public abstract class ChunkStatusMixin implements ChunkStatusAccessor {
    @Shadow public abstract String toString();

    @Unique
    private byte[] idBytes;

    @Override
    public byte[] getIdBytes() {
        return this.idBytes != null ? this.idBytes : (this.idBytes = NbtWriter.getStringBytes(this.toString()));
    }
}
