package com.ishland.c2me.base.mixin.access;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FlowableFluid.class)
public interface IFlowableFluid {

    @Invoker
    int invokeGetLevelDecreasePerBlock(WorldView world);

    @Invoker
    boolean invokeReceivesFlow(Direction face, BlockView world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState);

    @Invoker
    boolean invokeIsMatchingAndStill(FluidState state);

}
