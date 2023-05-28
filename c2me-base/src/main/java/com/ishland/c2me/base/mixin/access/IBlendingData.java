package com.ishland.c2me.base.mixin.access;

import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.chunk.BlendingData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(value = BlendingData.class)
public interface IBlendingData {
    @Accessor
    HeightLimitView getOldHeightLimit();

    @Accessor
    double[] getSurfaceHeights();
}
