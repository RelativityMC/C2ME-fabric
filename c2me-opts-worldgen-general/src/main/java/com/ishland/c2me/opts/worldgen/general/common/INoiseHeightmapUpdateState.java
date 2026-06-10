package com.ishland.c2me.opts.worldgen.general.common;

import net.minecraft.block.BlockState;
import net.minecraft.world.Heightmap;

public interface INoiseHeightmapUpdateState {

    void c2me$initNoiseHeightmapUpdateState();

    void c2me$clearNoiseHeightmapUpdateState();

    boolean c2me$trackNoiseHeightmapUpdate(Heightmap heightmap, int x, int y, int z, BlockState state);
}
