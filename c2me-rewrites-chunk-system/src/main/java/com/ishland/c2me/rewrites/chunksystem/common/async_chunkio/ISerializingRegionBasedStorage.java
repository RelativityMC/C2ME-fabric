package com.ishland.c2me.rewrites.chunksystem.common.async_chunkio;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;

public interface ISerializingRegionBasedStorage {

    void update(ChunkPos pos, NbtCompound tag);

}
