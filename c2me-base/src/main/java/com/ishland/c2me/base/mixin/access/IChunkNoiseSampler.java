package com.ishland.c2me.base.mixin.access;

import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkNoiseSampler.class)
public interface IChunkNoiseSampler {

    @Accessor
    int getHorizontalBlockSize();

    @Accessor
    int getVerticalBlockSize();

    @Accessor("startBlockX")
    int getBaseX();

    @Accessor("startBlockY")
    int getBaseY();

    @Accessor("startBlockZ")
    int getBaseZ();

    @Accessor("cellBlockX")
    int getOffsetX();

    @Accessor("cellBlockY")
    int getOffsetY();

    @Accessor("cellBlockZ")
    int getOffsetZ();

    @Accessor("minimumCellY")
    int getMinimumY();

    @Accessor("cellHeight")
    int getHeight();

    @Accessor
    int getBiomeHorizontalEnd();

    @Accessor
    int getBiomeX();

    @Accessor
    int getBiomeZ();

}
