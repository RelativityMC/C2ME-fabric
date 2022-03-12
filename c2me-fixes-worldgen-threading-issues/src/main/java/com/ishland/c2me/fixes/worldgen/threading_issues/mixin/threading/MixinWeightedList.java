package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import com.ishland.c2me.base.mixin.access.IWeightedListEntry;
import com.ishland.c2me.fixes.worldgen.threading_issues.common.IWeightedList;
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
    @SuppressWarnings("unchecked")
    @Overwrite
    public WeightedList<U> shuffle() {
        // TODO [VanillaCopy]
        final WeightedList<U> newList = new WeightedList<>(entries); // C2ME - use new instance
        final Random random = new Random(); // C2ME - use new instance
        ((com.ishland.c2me.base.mixin.access.IWeightedList<U>) newList).getEntries().forEach((entry) -> { // C2ME - use new instance
            ((IWeightedListEntry) entry).invokeSetShuffledOrder(random.nextFloat());
        });
        ((com.ishland.c2me.base.mixin.access.IWeightedList<U>) newList).getEntries().sort(Comparator.comparingDouble((object) -> { // C2ME - use new instance
            return ((IWeightedListEntry)object).invokeGetShuffledOrder();
        }));
        return newList; // C2ME - use new instance
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @Override
    public WeightedList<U> shuffleVanilla() {
        // TODO [VanillaCopy]
        this.entries.forEach((entry) -> {
            ((IWeightedListEntry) entry).invokeSetShuffledOrder(this.random.nextFloat());
        });
        this.entries.sort(Comparator.comparingDouble(uEntry -> ((IWeightedListEntry) uEntry).invokeGetShuffledOrder()));
        return (WeightedList<U>) (Object) this;
    }
}
