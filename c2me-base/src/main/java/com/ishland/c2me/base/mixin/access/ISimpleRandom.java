package com.ishland.c2me.base.mixin.access;

import net.minecraft.util.math.random.LocalRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LocalRandom.class)
public interface ISimpleRandom {

    @Accessor
    long getSeed();

    @Invoker
    void invokeSetSeed(long seed);

}
