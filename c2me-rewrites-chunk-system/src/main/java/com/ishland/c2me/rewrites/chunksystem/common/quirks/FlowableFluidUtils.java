/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.rewrites.chunksystem.common.quirks;

import com.ishland.c2me.base.mixin.access.IFlowableFluid;
import it.unimi.dsi.fastutil.shorts.Short2BooleanFunction;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectFunction;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
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

    public static boolean needsPostProcessing(WorldView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (!fluidState.isStill()) {
            return true;
        }
        if (fluidState.isEmpty()) {
            return false;
        }
        return canFormBubbleColumn(world, pos, blockState, fluidState) || canFlowNormally(world, pos, blockState, fluidState);
    }

    private static boolean canFormBubbleColumn(WorldView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        BlockPos belowPos = pos.down();
        BlockState belowBlockState = world.getBlockState(belowPos);
        if (belowBlockState.isIn(BlockTags.ENABLES_BUBBLE_COLUMN_DRAG_DOWN) || belowBlockState.isIn(BlockTags.ENABLES_BUBBLE_COLUMN_PUSH_UP)) {
            return true;
        }
        return false;
    }

    private static boolean canFlowNormally(WorldView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        BlockPos belowPos = pos.down();
        BlockState belowBlockState = world.getBlockState(belowPos);
        FluidState belowFluidState = belowBlockState.getFluidState();
        // very rough filtering
        if (((IFlowableFluid) fluidState.getFluid()).invokeCanFlowThrough(world, pos, blockState, Direction.DOWN, belowPos, belowBlockState, belowFluidState)) {
            FluidState fluidState3 = getUpdatedState(((FlowableFluid) fluidState.getFluid()), world, belowPos, belowBlockState);
            if (fluidState3 == null) {
                return true; // shortcut
            }
            Fluid fluid = fluidState3.getFluid();
            if (belowFluidState.canBeReplacedWith(world, belowPos, fluid, Direction.DOWN) && IFlowableFluid.invokeCanFillWithFluid(world, belowPos, belowBlockState, fluid)) {
                return true;
            }
        }
        if ((fluidState.isStill() || !(((IFlowableFluid) fluidState.getFluid()).invokeCanFlowDownTo(world, pos, blockState, belowPos, belowBlockState))) &&
                canSpreadToSidesNormally(world, pos, blockState, fluidState)) {
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
                if (((IFlowableFluid) fluidState.getFluid()).invokeCanFlowThrough(world, pos, blockState, direction, offsetPos, offsetBlockState, offsetFluidState)) {
                    FluidState fluidState2 = getUpdatedState((FlowableFluid) fluidState.getFluid(), world, offsetPos, offsetBlockState);
                    if (fluidState2 == null) {
                        return true; // shortcut
                    }
                    if (IFlowableFluid.invokeCanFillWithFluid(world, offsetPos, offsetBlockState, fluidState2.getFluid())) {
                        return true; // shortcut
                    }
                }
            }
        }

        return false;
    }

    private static FluidState getUpdatedState(FlowableFluid receiver, WorldView world, BlockPos pos, BlockState state) {
        int i = 0;
        int j = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos blockPos = mutable.set(pos, direction);
            BlockState blockState = world.getBlockState(blockPos);
            FluidState fluidState = blockState.getFluidState();
            if (fluidState.getFluid().matchesType(receiver) && IFlowableFluid.invokeReceivesFlow(direction, world, pos, state, blockPos, blockState)) {
                if (fluidState.isStill()) {
                    j++;
                }

                i = Math.max(i, fluidState.getLevel());
            }
        }

//        if (j >= 2 && this.isInfinite(world)) {
//            BlockState blockState2 = world.getBlockState(mutable.set(pos, Direction.DOWN));
//            FluidState fluidState2 = blockState2.getFluidState();
//            if (blockState2.isSolid() || receiver.isMatchingAndStill(fluidState2)) {
//                return receiver.getStill(false);
//            }
//        }
        if (j >= 2) {
            return null; // to not filter this
        }

        BlockPos blockPos2 = mutable.set(pos, Direction.UP);
        BlockState blockState3 = world.getBlockState(blockPos2);
        FluidState fluidState3 = blockState3.getFluidState();
        if (!fluidState3.isEmpty() && fluidState3.getFluid().matchesType(receiver) && IFlowableFluid.invokeReceivesFlow(Direction.UP, world, pos, state, blockPos2, blockState3)) {
            return receiver.getFlowing(8, true);
        } else {
            int k = i - ((IFlowableFluid) receiver).invokeGetLevelDecreasePerBlock(world);
            return k <= 0 ? Fluids.EMPTY.getDefaultState() : receiver.getFlowing(k, false);
        }
    }

    private static class SpreadCache {
        private final FlowableFluid flowableFluid;
        private final BlockView world;
        private final BlockPos startPos;
        private final Short2ObjectMap<BlockState> stateCache = new Short2ObjectOpenHashMap<>();
        private final Short2BooleanMap flowDownCache = new Short2BooleanOpenHashMap();

        SpreadCache(final FlowableFluid flowableFluid, final BlockView world, final BlockPos startPos) {
            this.flowableFluid = flowableFluid;
            this.world = world;
            this.startPos = startPos;
        }

        public BlockState getBlockState(BlockPos pos) {
            return this.getBlockState(pos, this.pack(pos));
        }

        private BlockState getBlockState(BlockPos pos, short packed) {
            return this.stateCache.computeIfAbsent(packed, (Short2ObjectFunction<? extends BlockState>)(packedPos -> this.world.getBlockState(pos)));
        }

        public boolean canFlowDownTo(BlockPos pos) {
            return this.flowDownCache.computeIfAbsent(this.pack(pos), (Short2BooleanFunction)(packed -> {
                BlockState blockState = this.getBlockState(pos, packed);
                BlockPos blockPos2 = pos.down();
                BlockState blockState2 = this.world.getBlockState(blockPos2);
                return ((IFlowableFluid) this.flowableFluid).invokeCanFlowDownTo(this.world, pos, blockState, blockPos2, blockState2);
            }));
        }

        private short pack(BlockPos pos) {
            int i = pos.getX() - this.startPos.getX();
            int j = pos.getZ() - this.startPos.getZ();
            return (short)((i + 128 & 0xFF) << 8 | j + 128 & 0xFF);
        }
    }

}
