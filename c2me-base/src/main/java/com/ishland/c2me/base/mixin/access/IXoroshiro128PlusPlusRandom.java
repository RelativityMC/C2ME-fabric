package com.ishland.c2me.base.mixin.access;

import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandomImpl;
import net.minecraft.world.gen.random.Xoroshiro128PlusPlusRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Xoroshiro128PlusPlusRandom.class)
public interface IXoroshiro128PlusPlusRandom {

    @Accessor
    Xoroshiro128PlusPlusRandomImpl getImplementation();

}
