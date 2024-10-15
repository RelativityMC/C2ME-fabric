package com.ishland.c2me.base.mixin.access;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DensityFunctionTypes.WeirdScaledSampler.RarityValueMapper.class)
public interface IDensityFunctionTypesWeirdScaledSamplerRarityValueMapper {

    @Accessor
    Double2DoubleFunction getScaleFunction();

}
