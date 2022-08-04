package com.ishland.c2me.opts.chunk_serializer.mixin;

import com.ishland.c2me.opts.chunk_serializer.common.NbtWriter;
import com.ishland.c2me.opts.chunk_serializer.common.utils.StringBytesConvertible;
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
