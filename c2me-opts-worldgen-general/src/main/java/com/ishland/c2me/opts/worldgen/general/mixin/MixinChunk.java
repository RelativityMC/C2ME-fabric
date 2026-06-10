package com.ishland.c2me.opts.worldgen.general.mixin;

import com.ishland.c2me.opts.worldgen.general.common.INoiseHeightmapUpdateState;
import net.minecraft.block.BlockState;
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
    public boolean c2me$trackNoiseHeightmapUpdate(Heightmap heightmap, int x, int y, int z, BlockState state) {
        if (heightmap == this.c2me$oceanFloorHeightmap) {
            return this.c2me$trackOnce(heightmap, this.c2me$oceanFloorHeightmapDone, x, y, z, state);
        }
        if (heightmap == this.c2me$worldSurfaceHeightmap) {
            return this.c2me$trackOnce(heightmap, this.c2me$worldSurfaceHeightmapDone, x, y, z, state);
        }
        return heightmap.trackUpdate(x, y, z, state);
    }

    @Unique
    private boolean c2me$trackOnce(Heightmap heightmap, long[] doneBits, int x, int y, int z, BlockState state) {
        int index = z << 4 | x;
        int word = index >>> 6;
        long mask = 1L << index;
        if ((doneBits[word] & mask) != 0L) {
            return false;
        }
        boolean updated = heightmap.trackUpdate(x, y, z, state);
        if (updated) {
            doneBits[word] |= mask;
        }
        return updated;
    }
}
