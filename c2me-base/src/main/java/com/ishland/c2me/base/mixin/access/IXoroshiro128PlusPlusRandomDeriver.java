package com.ishland.c2me.base.mixin.access;

import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Xoroshiro128PlusPlusRandom.Splitter.class)
public interface IXoroshiro128PlusPlusRandomDeriver {

    @Accessor
    long getSeedLo();

    @Accessor
    long getSeedHi();

}
