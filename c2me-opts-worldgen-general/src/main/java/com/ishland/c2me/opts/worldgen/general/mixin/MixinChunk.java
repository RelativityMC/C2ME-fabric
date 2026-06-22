package com.ishland.c2me.opts.worldgen.general.mixin;

import com.ishland.c2me.opts.worldgen.general.common.INoiseHeightmapUpdateState;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Arrays;

@Mixin(Chunk.class)
public abstract class MixinChunk implements INoiseHeightmapUpdateState {

    @Unique
    private final long[] c2me$oceanFloorHeightmapDone = new long[4];
    @Unique
    private final long[] c2me$worldSurfaceHeightmapDone = new long[4];
    @Unique
    private Heightmap c2me$oceanFloorHeightmap;
    @Unique
    private Heightmap c2me$worldSurfaceHeightmap;

    @Override
    public void c2me$initNoiseHeightmapUpdateState() {
        Chunk chunk = (Chunk) (Object) this;
        this.c2me$oceanFloorHeightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        this.c2me$worldSurfaceHeightmap = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        Arrays.fill(this.c2me$oceanFloorHeightmapDone, 0L);
        Arrays.fill(this.c2me$worldSurfaceHeightmapDone, 0L);
    }

    @Override
    public void c2me$clearNoiseHeightmapUpdateState() {
        this.c2me$oceanFloorHeightmap = null;
        this.c2me$worldSurfaceHeightmap = null;
    }

    @Override
    public long[] c2me$noiseHeightmapDoneBits(Heightmap heightmap) {
        if (heightmap == this.c2me$oceanFloorHeightmap) {
            return this.c2me$oceanFloorHeightmapDone;
        }
        if (heightmap == this.c2me$worldSurfaceHeightmap) {
            return this.c2me$worldSurfaceHeightmapDone;
        }
        return null;
    }
}
