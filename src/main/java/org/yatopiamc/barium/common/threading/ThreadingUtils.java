package org.yatopiamc.barium.common.threading;

import net.minecraft.world.chunk.ChunkStatus;

public class ThreadingUtils {

    public static ThreadingType getThreadingType(final ChunkStatus status) {
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
                return ThreadingType.PARALLELIZED;
            case "features":
                return ThreadingType.SINGLE_THREADED;
            default:
                return ThreadingType.AS_IS;
        }
    }

}
