package com.ishland.c2me.base.mixin.access;

import net.minecraft.world.gen.random.AtomicSimpleRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AtomicSimpleRandom.RandomDeriver.class)
public interface IAtomicSimpleRandomDeriver {

    @Accessor
    long getSeed();

}
