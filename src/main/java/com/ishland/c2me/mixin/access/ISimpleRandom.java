package com.ishland.c2me.mixin.access;

import net.minecraft.world.gen.random.SimpleRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleRandom.class)
public interface ISimpleRandom {

    @Accessor
    long getSeed();

    @Accessor
    void setSeed(long seed);

}
