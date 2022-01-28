package com.ishland.c2me.mixin.access;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.IOException;
import java.nio.ByteBuffer;

@Mixin(RegionFile.class)
public interface IRegionFile {

    @Invoker
    void invokeWriteChunk(ChunkPos pos, ByteBuffer byteBuffer) throws IOException;

}
