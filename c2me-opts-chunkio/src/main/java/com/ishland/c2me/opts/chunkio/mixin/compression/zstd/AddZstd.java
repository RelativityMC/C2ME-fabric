package com.ishland.c2me.opts.chunkio.mixin.compression.zstd;

import net.minecraft.world.storage.ChunkStreamVersion;
import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkStreamVersion.class)
public class AddZstd {

    @Invoker("add")
    public static ChunkStreamVersion add(ChunkStreamVersion version) {
        return null;
    }

    private static final ChunkStreamVersion zstd = add(new ChunkStreamVersion(4, inputStream -> new ZstdInputStream(inputStream), outputStream ->new ZstdOutputStream(outputStream)));
    
}
