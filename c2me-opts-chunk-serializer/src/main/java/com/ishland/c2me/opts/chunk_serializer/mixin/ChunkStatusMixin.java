package com.ishland.c2me.opts.chunk_serializer.mixin;

import com.ishland.c2me.opts.chunk_serializer.common.ChunkStatusAccessor;
import com.ishland.c2me.opts.chunk_serializer.common.NbtWriter;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;

@Mixin(ChunkStatus.class)
public class ChunkStatusMixin implements ChunkStatusAccessor {
    private byte[] idBytes;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(String id, ChunkStatus previous, int taskMargin, EnumSet<?> heightMapTypes, ChunkStatus.ChunkType chunkType, ChunkStatus.GenerationTask generationTask, ChunkStatus.LoadTask loadTask, CallbackInfo ci) {
        this.idBytes = NbtWriter.getAsciiStringBytes(id);
    }

    @Override
    public byte[] getIdBytes() {
        return this.idBytes;
    }
}
