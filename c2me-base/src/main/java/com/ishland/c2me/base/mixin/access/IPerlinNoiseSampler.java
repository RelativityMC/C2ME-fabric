package com.ishland.c2me.base.mixin.access;

import net.minecraft.util.math.noise.PerlinNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PerlinNoiseSampler.class)
public interface IPerlinNoiseSampler {

    @Accessor
    byte[] getPermutations();

}
