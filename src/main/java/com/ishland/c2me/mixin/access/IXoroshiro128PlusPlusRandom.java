package com.ishland.c2me.mixin.access;

import net.minecraft.world.gen.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.gen.random.Xoroshiro128PlusPlusRandomImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Xoroshiro128PlusPlusRandom.class)
public interface IXoroshiro128PlusPlusRandom {

    @Accessor
    Xoroshiro128PlusPlusRandomImpl getImplementation();

}
