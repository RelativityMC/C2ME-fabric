package com.ishland.c2me.rewrites.chunksystem.mixin.fluid_postprocessing;

import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldChunk.class)
public class MixinWorldChunk {

    @Redirect(method = "runPostProcessing", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;onScheduledTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
    private void redirectFluidScheduledTick(FluidState instance, World world, BlockPos pos) {
        world.scheduleFluidTick(pos, instance.getFluid(), 1);
    }

}
