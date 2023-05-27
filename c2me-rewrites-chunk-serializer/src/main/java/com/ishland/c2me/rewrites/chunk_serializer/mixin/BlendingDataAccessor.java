package com.ishland.c2me.rewrites.chunk_serializer.mixin;

import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.chunk.BlendingData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(value = BlendingData.class)
public interface BlendingDataAccessor {
    @Accessor
    HeightLimitView getOldHeightLimit();

    @Accessor
    double[] getSurfaceHeights();
}
