package org.yatopiamc.c2me.mixin.optimization.worldgen.threadsafe_weightedlist;

import net.minecraft.entity.ai.brain.task.CompositeTask;
import net.minecraft.util.collection.WeightedList;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.yatopiamc.c2me.common.optimization.worldgen.threadsafe_weightedlist.IWeightedList;

@Mixin(CompositeTask.Order.class)
public class MixinOrder {

    @Dynamic
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/WeightedList;shuffle()Lnet/minecraft/util/collection/WeightedList;"))
    private WeightedList<?> redirectShuffle(WeightedList<?> obj) {
        return ((IWeightedList<?>) obj).shuffleVanilla();
    }

}
