package com.ishland.c2me.opts.chunkio.mixin.compression.modify_default_chunk_compression;

import com.ishland.c2me.opts.chunkio.common.ConfigConstants;
import net.minecraft.world.storage.ChunkStreamVersion;
import net.minecraft.world.storage.RegionFile;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RegionFile.class)
public class MixinRegionFile {

    @Redirect(method = "<init>(Ljava/nio/file/Path;Ljava/nio/file/Path;Z)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/storage/ChunkStreamVersion;DEFLATE:Lnet/minecraft/world/storage/ChunkStreamVersion;", opcode = Opcodes.GETSTATIC))
    private static ChunkStreamVersion redirectDefaultChunkStreamVersion() {
        return ConfigConstants.CHUNK_STREAM_VERSION;
    }

}
