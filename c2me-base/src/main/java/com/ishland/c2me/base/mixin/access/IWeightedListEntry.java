package com.ishland.c2me.base.mixin.access;

import net.minecraft.util.collection.ShufflingList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ShufflingList.Entry.class)
public interface IWeightedListEntry {

    @Invoker
    double invokeGetShuffledOrder();

    @Invoker
    void invokeSetShuffledOrder(float random);

}
