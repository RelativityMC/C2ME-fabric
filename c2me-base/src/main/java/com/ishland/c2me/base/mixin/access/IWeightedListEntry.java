package com.ishland.c2me.base.mixin.access;

import net.minecraft.util.collection.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WeightedList.Entry.class)
public interface IWeightedListEntry {

    @Invoker
    double invokeGetShuffledOrder();

    @Invoker
    void invokeSetShuffledOrder(float random);

}
