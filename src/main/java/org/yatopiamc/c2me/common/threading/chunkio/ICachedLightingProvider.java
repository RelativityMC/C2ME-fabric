package org.yatopiamc.c2me.common.threading.chunkio;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

public interface ICachedLightingProvider {

    void prepareLightData(ChunkPos pos);

    LightData takeLightData(ChunkSectionPos pos);

    public static final class LightData {
        public final byte[] blockLight;
        public final byte[] skyLight;

        public LightData(byte[] blockLight, byte[] skyLight) {
            this.blockLight = blockLight;
            this.skyLight = skyLight;
        }
    }

}
