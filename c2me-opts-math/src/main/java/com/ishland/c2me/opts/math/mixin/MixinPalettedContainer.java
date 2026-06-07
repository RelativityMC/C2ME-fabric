package com.ishland.c2me.opts.math.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PalettedContainer.class)
public abstract class MixinPalettedContainer {

    // devirtualizes the two vanilla providers on the palette read hot path; unknown providers
    // (other mods) fall through to the original virtual call
    @WrapOperation(
            method = "get(III)Ljava/lang/Object;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/PalettedContainer$PaletteProvider;computeIndex(III)I"),
            require = 0
    )
    private int fastComputeIndex(PalettedContainer.PaletteProvider provider, int x, int y, int z, Operation<Integer> original) {
        if (provider == PalettedContainer.PaletteProvider.BLOCK_STATE) {
            return y << 8 | z << 4 | x;
        }
        if (provider == PalettedContainer.PaletteProvider.BIOME) {
            return y << 4 | z << 2 | x;
        }
        return original.call(provider, x, y, z);
    }
}
