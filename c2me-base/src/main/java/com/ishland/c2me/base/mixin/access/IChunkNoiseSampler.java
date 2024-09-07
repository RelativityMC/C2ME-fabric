package com.ishland.c2me.base.mixin.access;

import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkNoiseSampler.class)
public interface IChunkNoiseSampler {

    @Accessor
    int getStartBlockX();

    @Accessor
    int getStartBlockY();

    @Accessor
    int getStartBlockZ();

    @Accessor
    int getHorizontalCellBlockCount();

    @Accessor
    int getVerticalCellBlockCount();

    @Accessor
    boolean getIsInInterpolationLoop();

    @Accessor
    boolean getIsSamplingForCaches();

    @Accessor
    int getStartBiomeX();

    @Accessor
    int getStartBiomeZ();

}
