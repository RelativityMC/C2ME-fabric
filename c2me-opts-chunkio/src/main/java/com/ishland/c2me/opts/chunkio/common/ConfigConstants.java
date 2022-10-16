package com.ishland.c2me.opts.chunkio.common;

import net.minecraft.world.storage.ChunkStreamVersion;
import org.spongepowered.asm.mixin.MixinEnvironment;

// Don't load this too early
public class ConfigConstants {

    public static final ChunkStreamVersion CHUNK_STREAM_VERSION;

    static {
        if (MixinEnvironment.getCurrentEnvironment().getPhase() != MixinEnvironment.Phase.DEFAULT) throw new IllegalStateException("Mixins not initialized yet");

        if (Config.chunkStreamVersion == -1) {
            CHUNK_STREAM_VERSION = ChunkStreamVersion.DEFLATE;
        } else {
            final ChunkStreamVersion chunkStreamVersion = ChunkStreamVersion.get((int) Config.chunkStreamVersion);
            if (Config.chunkStreamVersion != 4 && chunkStreamVersion == null) {
                Config.LOGGER.warn("Unknown compression {}, using vanilla default instead", Config.chunkStreamVersion);
                CHUNK_STREAM_VERSION = ChunkStreamVersion.DEFLATE;
            } else {
                CHUNK_STREAM_VERSION = chunkStreamVersion;
            }
        }
    }

}
