package com.ishland.c2me.base.mixin.access.fapi;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.fabricmc.fabric.impl.base.event.ArrayBackedEvent", remap = false)
public interface IArrayBackedEvent<T> {

    @Accessor("handlers")
    T[] c2me$getHandlers();

}
