package com.ishland.c2me.base.mixin.access;

import net.minecraft.util.math.noise.SimplexNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimplexNoiseSampler.class)
public interface ISimplexNoiseSampler {

    @Accessor
    int[] getPermutation();

}
