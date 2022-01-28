package com.ishland.c2me.common.config;

import com.ishland.c2me.C2MEMod;
import net.minecraft.world.storage.ChunkStreamVersion;

// Don't load this too early
public class C2MEConfigConstants {

    public static final ChunkStreamVersion CHUNK_STREAM_VERSION;

    static {
        if (C2MEConfig.generalOptimizationsConfig.chunkStreamVersion == -1) {
            CHUNK_STREAM_VERSION = ChunkStreamVersion.DEFLATE;
        } else {
            final ChunkStreamVersion chunkStreamVersion = ChunkStreamVersion.get(C2MEConfig.generalOptimizationsConfig.chunkStreamVersion);
            if (chunkStreamVersion == null) {
                C2MEMod.LOGGER.warn("Unknown compression {}, using vanilla default instead", C2MEConfig.generalOptimizationsConfig.chunkStreamVersion);
                CHUNK_STREAM_VERSION = ChunkStreamVersion.DEFLATE;
            } else {
                CHUNK_STREAM_VERSION = chunkStreamVersion;
            }
        }
    }

}
