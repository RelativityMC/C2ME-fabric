package com.ishland.c2me.opts.chunkio.mixin.compression.increase_buffer_size;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import net.minecraft.world.storage.ChunkStreamVersion;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@SuppressWarnings("InvalidInjectorMethodSignature")
@Mixin(ChunkStreamVersion.class)
public class MixinChunkStreamVersion {

    @SuppressWarnings({"InvalidMemberReference", "MixinAnnotationTarget"})
    @Dynamic
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "(ILnet/minecraft/world/storage/ChunkStreamVersion$Wrapper;Lnet/minecraft/world/storage/ChunkStreamVersion$Wrapper;)Lnet/minecraft/world/storage/ChunkStreamVersion;"))
    private static ChunkStreamVersion redirectChunkStreamVersionConstructor(int id, ChunkStreamVersion.Wrapper<InputStream> inputStreamWrapper, ChunkStreamVersion.Wrapper<OutputStream> outputStreamWrapper) {
        if (id == 1) { // GZIP
            return new ChunkStreamVersion(id, in -> new GZIPInputStream(in, 16 * 1024), out -> new GZIPOutputStream(out, 16 * 1024));
        } else if (id == 2) { // DEFLATE
            return new ChunkStreamVersion(id, in -> new InflaterInputStream(in, new Inflater(), 16 * 1024), out -> new DeflaterOutputStream(out, new Deflater(), 16 * 1024));
        } else if (id == 3) { // UNCOMPRESSED
            return new ChunkStreamVersion(id, BufferedInputStream::new, BufferedOutputStream::new);
        } else if (id == 4) { // zstd
            return new ChunkStreamVersion(id, in -> new ZstdInputStream(in), out -> new ZstdOutputStream(out));
        } else {
            return new ChunkStreamVersion(id, inputStreamWrapper, outputStreamWrapper);
        }
    }

}
