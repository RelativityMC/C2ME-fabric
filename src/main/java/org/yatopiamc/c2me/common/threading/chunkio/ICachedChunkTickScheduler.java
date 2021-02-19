package org.yatopiamc.c2me.common.threading.chunkio;

import net.minecraft.nbt.ListTag;

public interface ICachedChunkTickScheduler {

    void prepareCachedNbt();

    ListTag getCachedNbt();

}
