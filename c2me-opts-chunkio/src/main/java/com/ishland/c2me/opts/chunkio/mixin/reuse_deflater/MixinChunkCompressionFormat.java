package com.ishland.c2me.opts.chunkio.mixin.reuse_deflater;

import com.ishland.c2me.opts.chunkio.common.Config;
import com.ishland.c2me.opts.chunkio.common.DeflaterPool;
import net.minecraft.world.storage.ChunkCompressionFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Routes deflate chunk writes through a reused per-thread Deflater instead of allocating one per write.
 *
 * Vanilla's DEFLATE format wraps each chunk write in a single-arg new DeflaterOutputStream(stream), which
 * allocates a fresh Deflater (native zlib init) and ends it on close (native end). This intercepts the
 * wrap call for the DEFLATE format only and returns a stream backed by a pooled deflater, removing the
 * per-write init and end. Other formats (gzip, lz4, uncompressed, custom) are left untouched. The produced
 * bytes are identical, so existing region files stay readable.
 */
@Mixin(ChunkCompressionFormat.class)
public class MixinChunkCompressionFormat {

    @Shadow @Final public static ChunkCompressionFormat DEFLATE;

    @Inject(method = "wrap(Ljava/io/OutputStream;)Ljava/io/OutputStream;", at = @At("HEAD"), cancellable = true)
    private void reuseDeflater(OutputStream stream, CallbackInfoReturnable<OutputStream> cir) throws IOException {
        if (Config.reuseDeflaters && (Object) this == DEFLATE) {
            cir.setReturnValue(DeflaterPool.wrap(stream));
        }
    }

}
