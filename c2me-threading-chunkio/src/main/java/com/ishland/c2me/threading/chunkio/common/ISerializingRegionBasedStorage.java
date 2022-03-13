package com.ishland.c2me.threading.chunkio.common;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;

public interface ISerializingRegionBasedStorage {

    void update(ChunkPos pos, NbtCompound tag);

}
