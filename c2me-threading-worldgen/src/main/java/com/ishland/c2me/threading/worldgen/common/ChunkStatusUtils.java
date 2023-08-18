package com.ishland.c2me.threading.worldgen.common;

import com.ishland.c2me.base.common.scheduler.NeighborLockingUtils;
import net.minecraft.world.chunk.ChunkStatus;

import static com.ishland.c2me.base.common.scheduler.NeighborLockingUtils.ChunkStatusThreadingType.AS_IS;
import static com.ishland.c2me.base.common.scheduler.NeighborLockingUtils.ChunkStatusThreadingType.PARALLELIZED;
import static com.ishland.c2me.base.common.scheduler.NeighborLockingUtils.ChunkStatusThreadingType.SINGLE_THREADED;

public class ChunkStatusUtils {

    public static NeighborLockingUtils.ChunkStatusThreadingType getThreadingType(final ChunkStatus status) {
        if (status.equals(ChunkStatus.STRUCTURE_STARTS)
            || status.equals(ChunkStatus.STRUCTURE_REFERENCES)
            || status.equals(ChunkStatus.BIOMES)
            || status.equals(ChunkStatus.NOISE)
            || status.equals(ChunkStatus.SPAWN)
            || status.equals(ChunkStatus.SURFACE)
            || status.equals(ChunkStatus.CARVERS)) {
            return PARALLELIZED;
        } else if (status.equals(ChunkStatus.FEATURES)) {
            return Config.allowThreadedFeatures ? PARALLELIZED : SINGLE_THREADED;
        } else if (status.equals(ChunkStatus.INITIALIZE_LIGHT) ||
                   status.equals(ChunkStatus.LIGHT)) {
            return AS_IS;
        }
        return AS_IS;
    }

}
