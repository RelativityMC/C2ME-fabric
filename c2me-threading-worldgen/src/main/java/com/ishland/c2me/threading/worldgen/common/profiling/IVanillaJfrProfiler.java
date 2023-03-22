package com.ishland.c2me.threading.worldgen.common.profiling;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiling.jfr.Finishable;
import net.minecraft.world.World;

public interface IVanillaJfrProfiler {

    public Finishable startChunkLoadSchedule(ChunkPos chunkPos, RegistryKey<World> world, String targetStatus);

}
