package org.yatopiamc.c2me.common.threading.chunkio;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.math.ChunkPos;

public interface ICachedServerTickScheduler {

    void prepareCachedNbt(ChunkPos pos);

    ListTag getCachedNbt(ChunkPos pos);
}
