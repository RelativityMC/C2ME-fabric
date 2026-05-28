package com.ishland.c2me.base.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.util.collection.ShufflingList;

@Mixin(ShufflingList.class)
public interface IWeightedList<U> {

    @Accessor
    List<ShufflingList.Entry<U>> getEntries();

}
