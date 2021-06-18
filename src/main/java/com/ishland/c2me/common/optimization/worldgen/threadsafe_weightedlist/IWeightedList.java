package com.ishland.c2me.common.optimization.worldgen.threadsafe_weightedlist;

import net.minecraft.util.collection.WeightedList;

public interface IWeightedList<U> {

    public WeightedList<U> shuffleVanilla();

}
