package com.ishland.c2me.opts.chunkio.common.async_chunk_on_player_login;

import net.minecraft.nbt.NbtCompound;

public interface IAsyncChunkPlayer {

    void markPlayerForAsyncChunkLoad();

    void setPlayerData(NbtCompound nbtCompound);

    NbtCompound getPlayerData();

    boolean isChunkLoadCompleted();

    void onChunkLoadComplete();

}
