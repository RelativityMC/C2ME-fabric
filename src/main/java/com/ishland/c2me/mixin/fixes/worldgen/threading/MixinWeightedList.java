package com.ishland.c2me.mixin.fixes.worldgen.threading;

import com.ishland.c2me.common.fixes.worldgen.threading.IWeightedList;
import net.minecraft.util.collection.WeightedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Mixin(WeightedList.class)
public class MixinWeightedList<U> implements IWeightedList<U> {

    @Shadow @Final public List<WeightedList.Entry<U>> entries;

    @Shadow @Final private Random random;

    /**
     * @author ishland
     * @reason create new instance on shuffling
     */
    @Overwrite
    public WeightedList<U> shuffle(Random unused) {
        // TODO [VanillaCopy]
        final WeightedList<U> newList = new WeightedList<>(entries); // C2ME - use new instance
        final Random random = new Random(); // C2ME - use new instance
        newList.entries.forEach((entry) -> { // C2ME - use new instance
            entry.setShuffledOrder(random.nextFloat());
        });
        newList.entries.sort(Comparator.comparingDouble((object) -> { // C2ME - use new instance
            return ((WeightedList.Entry)object).getShuffledOrder();
        }));
        return newList; // C2ME - use new instance
    }

    @Override
    public WeightedList<U> shuffleVanilla() {
        // TODO [VanillaCopy]
        this.entries.forEach((entry) -> {
            entry.setShuffledOrder(this.random.nextFloat());
        });
        this.entries.sort(Comparator.comparingDouble(WeightedList.Entry::getShuffledOrder));
        return (WeightedList<U> ) (Object) this;
    }
}
