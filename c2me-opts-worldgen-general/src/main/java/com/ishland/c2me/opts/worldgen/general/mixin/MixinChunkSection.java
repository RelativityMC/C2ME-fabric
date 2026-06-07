package com.ishland.c2me.opts.worldgen.general.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkSection.class)
public class MixinChunkSection {

    @WrapOperation(
            method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isEmpty()Z", ordinal = 0)
    )
    private boolean skipOldAirFluidEmptyCheck(FluidState instance, Operation<Boolean> original, @Local(ordinal = 1) BlockState oldState) {
        return oldState.isAir() || original.call(instance);
    }

    @WrapOperation(
            method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isEmpty()Z", ordinal = 1)
    )
    private boolean skipNewAirFluidEmptyCheck(FluidState instance, Operation<Boolean> original, @Local(argsOnly = true) BlockState newState) {
        return newState.isAir() || original.call(instance);
    }
}
