/*
 * All Rights Reserved
 *
 * Copyright (c) 2025-2026 ishland
 *
 * All rights reserved. Do not redistribute.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.ArrayList;
import java.util.Arrays;

public class CLBlockStateMappings {

    //static constant const int32_t BLOCK_NULL = 0;
    //static constant const int32_t BLOCK_AIR = 1;
    //static constant const int32_t BLOCK_DEFAULT_BLOCK = 2;
    //static constant const int32_t BLOCK_WATER = 3;
    //static constant const int32_t BLOCK_LAVA = 4;
    //static constant const int32_t BLOCK_COPPER_ORE = 5;
    //static constant const int32_t BLOCK_RAW_COPPER_BLOCK = 6;
    //static constant const int32_t BLOCK_GRANITE = 7;
    //static constant const int32_t BLOCK_DEEPSLATE_IRON_ORE = 8;
    //static constant const int32_t BLOCK_RAW_IRON_BLOCK = 9;
    //static constant const int32_t BLOCK_TUFF = 10;
    private static final BlockState[] DEFAULT_BLOCK_STATE_MAPPINGS = new BlockState[]{
            null,
            Blocks.AIR.getDefaultState(),
            null, // default block
            Blocks.WATER.getDefaultState(),
            Blocks.LAVA.getDefaultState(),
            Blocks.COPPER_ORE.getDefaultState(),
            Blocks.RAW_COPPER_BLOCK.getDefaultState(),
            Blocks.GRANITE.getDefaultState(),
            Blocks.DEEPSLATE_IRON_ORE.getDefaultState(),
            Blocks.RAW_IRON_BLOCK.getDefaultState(),
            Blocks.TUFF.getDefaultState(),
    };

    public static CLBlockStateMappings defaultMappings(BlockState defaultBlock, BlockState defaultFluid) {
        ArrayList<BlockState> states = new ArrayList<>(Arrays.asList(DEFAULT_BLOCK_STATE_MAPPINGS.clone()));
        if (!states.contains(defaultBlock)) {
            states.set(2, defaultBlock);
        }
        if (!states.contains(defaultFluid)) {
            states.add(defaultFluid);
        }
        return new CLBlockStateMappings(states.toArray(BlockState[]::new));
    }

    private final BlockState[] idToBlockState;
    private final Object2IntMap<BlockState> blockStateToId;

    public CLBlockStateMappings(BlockState[] idToBlockState) {
        this.idToBlockState = idToBlockState;
        Object2IntMap<BlockState> blockStateToId = new Object2IntOpenHashMap<>();
        BlockState[] toBlockState = this.idToBlockState;
        for (int i = 0, toBlockStateLength = toBlockState.length; i < toBlockStateLength; i++) {
            BlockState blockState = toBlockState[i];
            blockStateToId.put(blockState, i);
        }
        blockStateToId.defaultReturnValue(Integer.MAX_VALUE);
        this.blockStateToId = blockStateToId;
    }

    public int toId(BlockState blockState) {
        int id = this.blockStateToId.getInt(blockState);
        if (id == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("BlockState not found in mapping: " + blockState);
        }
        return id;
    }

    public BlockState[] getIdToBlockState() {
        return this.idToBlockState;
    }

    public Object2IntMap<BlockState> getBlockStateToId() {
        return this.blockStateToId;
    }

    public BlockState getBlockState(int value) {
//        if (value > this.idToBlockState.length) {
//            return Blocks.AIR.getDefaultState();
//        }
        return this.idToBlockState[value];
    }
}
