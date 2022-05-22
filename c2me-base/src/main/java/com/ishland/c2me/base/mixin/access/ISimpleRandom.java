package com.ishland.c2me.base.mixin.access;

import net.minecraft.util.math.random.LocalRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocalRandom.class)
public interface ISimpleRandom {

    @Accessor
    long getSeed();

    @Accessor
    void setSeed(long seed);

}
