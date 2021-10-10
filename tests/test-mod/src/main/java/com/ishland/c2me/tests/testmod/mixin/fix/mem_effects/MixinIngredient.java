package com.ishland.c2me.tests.testmod.mixin.fix.mem_effects;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Ingredient.class)
public abstract class MixinIngredient {

    @Shadow protected abstract void cacheMatchingStacks();

    @Shadow private ItemStack[] matchingStacks;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void onInit(CallbackInfo ci) {
        this.cacheMatchingStacks();
    }

}
