package org.yatopiamc.c2me.common.threading.chunkio;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;

public interface ISerializingRegionBasedStorage {

    void update(ChunkPos pos, CompoundTag tag);

}
