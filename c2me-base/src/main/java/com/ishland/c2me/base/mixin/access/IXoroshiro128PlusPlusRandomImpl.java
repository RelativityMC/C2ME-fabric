package com.ishland.c2me.base.mixin.access;

import net.minecraft.world.gen.random.Xoroshiro128PlusPlusRandomImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Xoroshiro128PlusPlusRandomImpl.class)
public interface IXoroshiro128PlusPlusRandomImpl {

    @Accessor
    long getSeedLo();

    @Accessor
    long getSeedHi();

    @Accessor
    void setSeedLo(long value);

    @Accessor
    void setSeedHi(long value);

}
