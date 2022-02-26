package com.ishland.c2me.tests.testmod.mixin.fix.remapper_being_broken;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.telepathicgrunt.repurposedstructures.utils.GeneralUtils")
public class MixinGeneralUtils {

    @Dynamic
    @Redirect(method = "lambda$enchantRandomly$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z"), remap = false)
    private static boolean redirectIsAcceptableItem(Enchantment instance, ItemStack stack) {
        try {
            return instance.isAcceptableItem(stack);
        } catch (Throwable t) {
            System.err.println(t.getMessage());
            return false;
        }
    }

}
