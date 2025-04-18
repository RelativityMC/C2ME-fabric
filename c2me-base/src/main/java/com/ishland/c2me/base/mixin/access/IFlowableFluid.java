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
    boolean invokeCanFlowThrough(BlockView world, BlockPos pos, BlockState state, Direction face, BlockPos fromPos, BlockState fromState, FluidState fluidState);

    @Invoker
    int invokeGetLevelDecreasePerBlock(WorldView world);

    @Invoker
    boolean invokeCanFlowDownTo(BlockView world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState);

    @Invoker
    public static boolean invokeCanFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        throw new AbstractMethodError();
    }

    @Invoker
    public static boolean invokeReceivesFlow(Direction face, BlockView world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
        throw new AbstractMethodError();
    }

}
