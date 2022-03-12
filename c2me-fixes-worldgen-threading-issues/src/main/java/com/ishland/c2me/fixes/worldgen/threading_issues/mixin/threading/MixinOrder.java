package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import com.ishland.c2me.fixes.worldgen.threading_issues.common.IWeightedList;
import net.minecraft.entity.ai.brain.task.CompositeTask;
import net.minecraft.util.collection.WeightedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(CompositeTask.Order.class)
public class MixinOrder {

    @Mutable
    @Shadow @Final private Consumer<WeightedList<?>> listModifier;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(String enumName, int ordinal, Consumer<WeightedList<?>> listModifier, CallbackInfo ci) {
        if (enumName.equals("field_18349") || enumName.equals("SHUFFLED"))
            this.listModifier = obj -> ((IWeightedList<?>) obj).shuffleVanilla();
    }

}
