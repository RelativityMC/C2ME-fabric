package com.ishland.c2me.rewrites.chunksystem.common.quirks;

import com.ishland.c2me.base.mixin.access.IFlowableFluid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FluidFillable;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public class FlowableFluidUtils {

    public static final ThreadLocal<Boolean> shouldntFlow = new ThreadLocal<>();
    public static final ThreadLocal<Boolean> hadFlown = new ThreadLocal<>();

    public static boolean needsPostProcessing(WorldView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (!fluidState.isStill()) {
            return true;
        }
        return canFlowNormally(world, pos, blockState, fluidState);
    }

    private static boolean canFlowNormally(WorldView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (fluidState.isEmpty()) return false;

        BlockPos belowPos = pos.down();
        BlockState belowBlockState = world.getBlockState(belowPos);
        FluidState belowFluidState = world.getFluidState(belowPos);
        // very rough filtering
        if (canFlowThrough1_21_5((FlowableFluid) fluidState.getFluid(), world, pos, blockState, Direction.DOWN, belowPos, belowBlockState, belowFluidState)) {
            FluidState fluidState3 = getUpdatedState(((FlowableFluid) fluidState.getFluid()), world, belowPos, belowBlockState);
            if (fluidState3 == null) {
                return true; // shortcut
            }
            Fluid fluid = fluidState3.getFluid();
            if (belowFluidState.canBeReplacedWith(world, belowPos, fluid, Direction.DOWN) && canFillWithFluid1_21_5(world, belowPos, belowBlockState, fluid)) {
                return true;
            }
        }
        if (canSpreadToSidesNormally(world, pos, blockState, fluidState)) { // fluid always still when reached here
            return true;
        }

        return false;
    }

    private static boolean canSpreadToSidesNormally(WorldView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        int nextFluidLevel = fluidState.getLevel() - ((IFlowableFluid) fluidState.getFluid()).invokeGetLevelDecreasePerBlock(world);
        if (fluidState.get(FlowableFluid.FALLING)) {
            nextFluidLevel = 7;
        }
        if (nextFluidLevel > 0) {
            // getSpread
//            int i = 1000;
//            Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
//            SpreadCache spreadCache = null;

            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockPos offsetPos = pos.offset(direction);
                BlockState offsetBlockState = world.getBlockState(offsetPos);
                FluidState offsetFluidState = offsetBlockState.getFluidState();
                if (canFlowThrough1_21_5((FlowableFluid) fluidState.getFluid(), world, pos, blockState, direction, offsetPos, offsetBlockState, offsetFluidState)) {
//                    FluidState fluidState2 = getUpdatedState((FlowableFluid) fluidState.getFluid(), world, offsetPos, offsetBlockState);
//                    if (fluidState2 == null) {
//                        return true; // shortcut
//                    }
//                    if (canFillWithFluid1_21_5(world, offsetPos, offsetBlockState, fluidState2.getFluid())) {
//                        return true; // shortcut
//                    }
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean canFlowThrough1_21_5(FlowableFluid receiver, BlockView world, BlockPos pos, BlockState state, Direction face, BlockPos fromPos, BlockState fromState, FluidState fluidState) {
        return !((IFlowableFluid) receiver).invokeIsMatchingAndStill(fluidState) &&
                canFillShort(fromState) &&
                ((IFlowableFluid) receiver).invokeReceivesFlow(face, world, pos, state, fromPos, fromState);
    }

    private static boolean canFill(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return canFillShort(state) && canFillWithFluid1_21_5(world, pos, state, fluid);
    }

    private static boolean canFillWithFluid1_21_5(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return state.getBlock() instanceof FluidFillable fluidFillable ? fluidFillable.canFillWithFluid(null, world, pos, state, fluid) : true;
    }

    private static boolean canFlowDownTo(FlowableFluid receiver, BlockView world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
        if (!((IFlowableFluid) receiver).invokeReceivesFlow(Direction.DOWN, world, pos, state, fromPos, fromState)) {
            return false;
        } else {
            return fromState.getFluidState().getFluid().matchesType(receiver) ? true : canFill(world, fromPos, fromState, receiver.getFlowing());
        }
    }

    private static boolean canFillShort(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof FluidFillable) {
            return true; // shortcut
        } else if (!(block instanceof DoorBlock)
                && !state.isIn(BlockTags.SIGNS)
                && !state.isOf(Blocks.LADDER)
                && !state.isOf(Blocks.SUGAR_CANE)
                && !state.isOf(Blocks.BUBBLE_COLUMN)) {
            return !state.isOf(Blocks.NETHER_PORTAL) &&
                    !state.isOf(Blocks.END_PORTAL) &&
                    !state.isOf(Blocks.END_GATEWAY) &&
                    !state.isOf(Blocks.STRUCTURE_VOID)
                    ? !state.blocksMovement()
                    : false;
        } else {
            return false;
        }
    }

    private static FluidState getUpdatedState(FlowableFluid receiver, WorldView world, BlockPos pos, BlockState state) {
        int i = 0;
        int j = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos blockPos = mutable.set(pos, direction);
            BlockState blockState = world.getBlockState(blockPos);
            FluidState fluidState = blockState.getFluidState();
            if (fluidState.getFluid().matchesType(receiver) && ((IFlowableFluid) receiver).invokeReceivesFlow(direction, world, pos, state, blockPos, blockState)) {
                if (fluidState.isStill()) {
                    j++;
                }

                i = Math.max(i, fluidState.getLevel());
            }
        }

        if (j >= 2) {
            return null; // to not filter this
        }

        BlockPos blockPos2 = mutable.set(pos, Direction.UP);
        BlockState blockState3 = world.getBlockState(blockPos2);
        FluidState fluidState3 = blockState3.getFluidState();
        if (!fluidState3.isEmpty() && fluidState3.getFluid().matchesType(receiver) && ((IFlowableFluid) receiver).invokeReceivesFlow(Direction.UP, world, pos, state, blockPos2, blockState3)) {
            return receiver.getFlowing(8, true);
        } else {
            int k = i - ((IFlowableFluid) receiver).invokeGetLevelDecreasePerBlock(world);
            return k <= 0 ? Fluids.EMPTY.getDefaultState() : receiver.getFlowing(k, false);
        }
    }

}
