package org.yatopiamc.c2me.common.threading.chunkio;

import net.minecraft.nbt.ListTag;

import java.util.concurrent.Executor;

public interface ICachedChunkTickScheduler {

    void setFallbackExecutor(Executor executor);

    void prepareCachedNbt();

    ListTag getCachedNbt();

}
