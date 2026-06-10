package com.ishland.c2me.opts.worldgen.general.common;

import net.minecraft.world.Heightmap;

public interface INoiseHeightmapUpdateState {

    void c2me$initNoiseHeightmapUpdateState();

    void c2me$clearNoiseHeightmapUpdateState();

    /**
     * @return the per-column done-bit words for the given worldgen heightmap, or {@code null}
     * if the heightmap is not tracked by the noise-stage update state
     */
    long[] c2me$noiseHeightmapDoneBits(Heightmap heightmap);
}
