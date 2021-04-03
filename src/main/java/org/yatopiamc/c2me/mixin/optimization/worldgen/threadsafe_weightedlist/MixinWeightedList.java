package org.yatopiamc.c2me.mixin.optimization.worldgen.threadsafe_weightedlist;

import net.minecraft.util.collection.WeightedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Mixin(WeightedList.class)
public class MixinWeightedList<U> {

    @Shadow @Final public List<WeightedList.Entry<U>> entries;

    /**
     * @author ishland
     * @reason create new instance on shuffling
     */
    @Overwrite
    public WeightedList<U> shuffle() {
        // TODO [VanillaCopy]
        final WeightedList<U> newList = new WeightedList<>(entries); // C2ME - use new instance
        newList.entries.forEach((entry) -> { // C2ME - use new instance
            entry.setShuffledOrder(new Random().nextFloat());
        });
        newList.entries.sort(Comparator.comparingDouble((object) -> { // C2ME - use new instance
            return ((WeightedList.Entry)object).getShuffledOrder();
        }));
        return newList; // C2ME - use new instance
    }

}
