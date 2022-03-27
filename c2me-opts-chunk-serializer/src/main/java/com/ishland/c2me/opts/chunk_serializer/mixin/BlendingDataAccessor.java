package com.ishland.c2me.opts.chunk_serializer.mixin;

import net.minecraft.world.gen.chunk.BlendingData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;


@Mixin(value = BlendingData.class)
public interface BlendingDataAccessor {
    @Accessor
    double[] getHeights();

    @Invoker
    boolean invokeUsesOldNoise();
}
