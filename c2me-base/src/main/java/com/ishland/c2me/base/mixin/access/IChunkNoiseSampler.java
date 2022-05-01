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

    @Accessor("field_36594")
    int getBaseX();

    @Accessor("field_36572")
    int getBaseY();

    @Accessor("field_36573")
    int getBaseZ();

    @Accessor("field_36574")
    int getOffsetX();

    @Accessor("field_36575")
    int getOffsetY();

    @Accessor("field_36576")
    int getOffsetZ();

    @Accessor
    int getMinimumY();

    @Accessor
    int getHeight();

}
