package org.yatopiamc.barium.common.threading.worldgen;

import net.minecraft.world.chunk.ChunkStatus;

public class ChunkStatusUtils {

    public static ChunkStatusThreadingType getThreadingType(final ChunkStatus status) {
        switch (status.getId()) {
            case "structure_starts":
            case "structure_references":
            case "biomes":
            case "noise":
            case "surface":
            case "carvers":
            case "liquid_carvers":
            case "spawn":
            case "heightmaps":
                return ChunkStatusThreadingType.PARALLELIZED;
            case "features":
                return ChunkStatusThreadingType.SINGLE_THREADED;
            default:
                return ChunkStatusThreadingType.AS_IS;
        }
    }

}
