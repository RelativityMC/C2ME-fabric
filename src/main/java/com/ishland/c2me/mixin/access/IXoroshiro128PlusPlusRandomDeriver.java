package com.ishland.c2me.mixin.access;

import net.minecraft.world.gen.random.Xoroshiro128PlusPlusRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Xoroshiro128PlusPlusRandom.RandomDeriver.class)
public interface IXoroshiro128PlusPlusRandomDeriver {

    @Accessor
    long getSeedLo();

    @Accessor
    long getSeedHi();

}
