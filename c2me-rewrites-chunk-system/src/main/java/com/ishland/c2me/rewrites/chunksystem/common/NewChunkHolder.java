package com.ishland.c2me.rewrites.chunksystem.common;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.light.LightingProvider;

public class NewChunkHolder extends ChunkHolder {

    public NewChunkHolder(ChunkPos pos, int level, HeightLimitView world, LightingProvider lightingProvider, LevelUpdateListener levelUpdateListener, PlayersWatchingChunkProvider playersWatchingChunkProvider) {
        super(pos, level, world, lightingProvider, levelUpdateListener, playersWatchingChunkProvider);
    }

}
