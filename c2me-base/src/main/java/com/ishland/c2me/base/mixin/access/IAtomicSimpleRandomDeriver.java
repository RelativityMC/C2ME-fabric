package com.ishland.c2me.base.mixin.access;

import net.minecraft.util.math.random.CheckedRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CheckedRandom.Splitter.class)
public interface IAtomicSimpleRandomDeriver {

    @Accessor
    long getSeed();

}
