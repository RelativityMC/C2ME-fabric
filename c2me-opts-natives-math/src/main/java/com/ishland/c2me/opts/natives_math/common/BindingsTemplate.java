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

package com.ishland.c2me.opts.natives_math.common;

import com.ishland.c2me.base.common.integration.lithostitched.FNLBindings;
import com.ishland.c2me.base.mixin.access.IInterpolatedNoiseSampler;
import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilSearchTree;
import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilSearchTreeTreeBranchNode;
import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilSearchTreeTreeLeafNode;
import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilSearchTreeTreeNode;
import com.ishland.c2me.base.mixin.access.IPerlinNoiseSampler;
import com.ishland.c2me.base.common.util.MemoryUtil;
import com.ishland.flowsched.util.Assertions;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class BindingsTemplate {

    //static inline void
    //math_noise_perlin_sample_legacy_area(const uint32_t *restrict const permutations,
    //                                     const double originX, const double originY, const double originZ,
    //                                     const double yScale, float *restrict const output,
    //                                     const int32_t sizeX, const int32_t sizeY, const int32_t sizeZ,
    //                                     const int32_t minBlockX, const int32_t minBlockY, const int32_t minBlockZ,
    //                                     const int32_t stepBlockX, const int32_t stepBlockY, const int32_t stepBlockZ,
    //                                     const double *restrict const shiftX, const double *restrict const shiftY,
    //                                     const double *restrict const shiftZ,
    //                                     const double scaleXz, const double scaleY, const float outputScale)

    public static final MethodHandle c2me_natives_noise_perlin_sample_legacy_area = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.ofVoid(
                    ValueLayout.ADDRESS,     // const uint32_t *restrict const permutations
                    ValueLayout.JAVA_DOUBLE, // const double originX
                    ValueLayout.JAVA_DOUBLE, // const double originY
                    ValueLayout.JAVA_DOUBLE, // const double originZ
                    ValueLayout.JAVA_DOUBLE, // const double yScale
                    ValueLayout.ADDRESS,     // float *restrict const output
                    ValueLayout.JAVA_INT,    // const int32_t sizeX
                    ValueLayout.JAVA_INT,    // const int32_t sizeY
                    ValueLayout.JAVA_INT,    // const int32_t sizeZ
                    ValueLayout.JAVA_INT,    // const int32_t minBlockX
                    ValueLayout.JAVA_INT,    // const int32_t minBlockY
                    ValueLayout.JAVA_INT,    // const int32_t minBlockZ
                    ValueLayout.JAVA_INT,    // const int32_t stepBlockX
                    ValueLayout.JAVA_INT,    // const int32_t stepBlockY
                    ValueLayout.JAVA_INT,    // const int32_t stepBlockZ
                    ValueLayout.ADDRESS,     // const double *restrict const shiftX
                    ValueLayout.ADDRESS,     // const double *restrict const shiftY
                    ValueLayout.ADDRESS,     // const double *restrict const shiftZ
                    ValueLayout.JAVA_DOUBLE, // const double scaleXz
                    ValueLayout.JAVA_DOUBLE, // const double scaleY
                    ValueLayout.JAVA_FLOAT   // const float outputScale
            ),
            Linker.Option.critical(true)
    );

    //static inline void
    //math_noise_perlin_sample_base_area(const uint32_t *restrict const permutations,
    //                                   const double originX, const double originY, const double originZ,
    //                                   float *restrict const output,
    //                                   const int32_t sizeX, const int32_t sizeY, const int32_t sizeZ,
    //                                   const int32_t minBlockX, const int32_t minBlockY, const int32_t minBlockZ,
    //                                   const int32_t stepBlockX, const int32_t stepBlockY, const int32_t stepBlockZ,
    //                                   const double *restrict const shiftX, const double *restrict const shiftY,
    //                                   const double *restrict const shiftZ,
    //                                   const double scaleXz, const double scaleY, const float outputScale)

    public static final MethodHandle c2me_natives_noise_perlin_sample_base_area = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.ofVoid(
                    ValueLayout.ADDRESS,     // const uint32_t *restrict const permutations
                    ValueLayout.JAVA_DOUBLE, // const double originX
                    ValueLayout.JAVA_DOUBLE, // const double originY
                    ValueLayout.JAVA_DOUBLE, // const double originZ
                    ValueLayout.ADDRESS,     // float *restrict const output
                    ValueLayout.JAVA_INT,    // const int32_t sizeX
                    ValueLayout.JAVA_INT,    // const int32_t sizeY
                    ValueLayout.JAVA_INT,    // const int32_t sizeZ
                    ValueLayout.JAVA_INT,    // const int32_t minBlockX
                    ValueLayout.JAVA_INT,    // const int32_t minBlockY
                    ValueLayout.JAVA_INT,    // const int32_t minBlockZ
                    ValueLayout.JAVA_INT,    // const int32_t stepBlockX
                    ValueLayout.JAVA_INT,    // const int32_t stepBlockY
                    ValueLayout.JAVA_INT,    // const int32_t stepBlockZ
                    ValueLayout.ADDRESS,     // const double *restrict const shiftX
                    ValueLayout.ADDRESS,     // const double *restrict const shiftY
                    ValueLayout.ADDRESS,     // const double *restrict const shiftZ
                    ValueLayout.JAVA_DOUBLE, // const double scaleXz
                    ValueLayout.JAVA_DOUBLE, // const double scaleY
                    ValueLayout.JAVA_FLOAT   // const float outputScale
            ),
            Linker.Option.critical(true)
    );

    // c2me_natives_end_islands_sample, float, (const int32_t *const simplex_permutations, const int32_t x, const int32_t z)
    public static final MethodHandle c2me_natives_end_islands_sample = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_FLOAT,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ),
            Linker.Option.critical(false)
    );
    public static final MethodHandle c2me_natives_end_islands_sample_ptr = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_FLOAT,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ),
            Linker.Option.critical(false)
    );

    // c2me_natives_biome_access_sample, uint32_t, (const int64_t theSeed, const int32_t x, const int32_t y, const int32_t z)
    public static final MethodHandle c2me_natives_biome_access_sample = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ),
            Linker.Option.critical(false)
    );

    // (const uint16_t *restrict const packedBlockPositions, uint32_t *restrict const res,
    //  const aquifer_data_t *restrict const aquiferData,
    //  const int32_t x, const int32_t y, const int32_t z)
    public static final MethodHandle c2me_natives_aquifer_refreshDistPosIdx = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.ofVoid(
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ),
            Linker.Option.critical(false)
    );

    public static final MethodHandle c2me_natives_aquifer_refreshDistPosIdx_ptr = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.ofVoid(
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
            ),
            Linker.Option.critical(false)
    );

    // uint32_t, (const biome_search_tree_node_t * restrict const nodes,
    // const uint32_t nodes_c, const uint32_t tree_depth,
    // uint16_t p0, uint16_t p1, uint16_t p2, uint16_t p3,
    // uint16_t p4, uint16_t p5, uint16_t p6)

    public static final MethodHandle c2me_natives_biome_search_tree_calc_args = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT
            ),
            Linker.Option.critical(false)
    );

    public static final MethodHandle c2me_natives_biome_search_tree_calc_args_ptr = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT,
                    ValueLayout.JAVA_SHORT
            ),
            Linker.Option.critical(false)
    );

    // typedef const struct biome_search_tree_node {
    //    // bit 31: set if branch node, clear if leaf node
    //    // bit 30: set if is branch node children offsets
    //    // bit 0-29: biome ID (only valid for leaf nodes)
    //    uint32_t state;
    //    union {
    //        struct {
    //            uint32_t children_offset[7]; // at most 7 children, 0 is reserved and means no child
    //        } branch_children;
    //        struct {
    //            int16_t maxs[7];
    //            int16_t mins[7];
    //        } node_minmaxs;
    //    };
    //} biome_search_tree_node_t;

    public static final StructLayout biome_search_tree_node = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("state"),
            MemoryLayout.unionLayout(
                    MemoryLayout.structLayout(
                            MemoryLayout.sequenceLayout(7, ValueLayout.JAVA_INT).withName("children_offset")
                    ).withName("branch_children"),
                    MemoryLayout.structLayout(
                            MemoryLayout.sequenceLayout(7, ValueLayout.JAVA_SHORT).withName("maxs"),
                            MemoryLayout.sequenceLayout(7, ValueLayout.JAVA_SHORT).withName("mins")
                    ).withName("node_minmaxs")
            ).withName("union0")
    ).withName("biome_search_tree_node_t");

    public static final VarHandle biome_search_tree_node$state = biome_search_tree_node.varHandle(MemoryLayout.PathElement.groupElement("state"));

    public static MemorySegment biome_search_tree_node$branch_children$children_offset(MemorySegment segment) {
        return segment.asSlice(biome_search_tree_node.byteOffset(MemoryLayout.PathElement.groupElement("union0"), MemoryLayout.PathElement.groupElement("branch_children"), MemoryLayout.PathElement.groupElement("children_offset")), 7 * ValueLayout.JAVA_INT.byteSize());
    }

    public static MemorySegment biome_search_tree_node$node_minmaxs$maxs(MemorySegment segment) {
        return segment.asSlice(biome_search_tree_node.byteOffset(MemoryLayout.PathElement.groupElement("union0"), MemoryLayout.PathElement.groupElement("node_minmaxs"), MemoryLayout.PathElement.groupElement("maxs")), 7 * ValueLayout.JAVA_SHORT.byteSize());
    }

    public static MemorySegment biome_search_tree_node$node_minmaxs$mins(MemorySegment segment) {
        return segment.asSlice(biome_search_tree_node.byteOffset(MemoryLayout.PathElement.groupElement("union0"), MemoryLayout.PathElement.groupElement("node_minmaxs"), MemoryLayout.PathElement.groupElement("mins")), 7 * ValueLayout.JAVA_SHORT.byteSize());
    }

    public static NativeBiomeSearchTree biome_search_tree_node$create(Arena arena, MultiNoiseUtil.SearchTree<RegistryEntry<Biome>> searchTree) {
        class TreeFlattener {
            private final List<SerializedTreeNode> nodes = new ArrayList<>();
            private final Object2IntLinkedOpenHashMap<RegistryEntry<Biome>> biomeIdMap = new Object2IntLinkedOpenHashMap<>();
            private int treeDepth;

            {
                this.biomeIdMap.defaultReturnValue(Integer.MIN_VALUE);
                this.nodes.add(new SerializedTreeNode()); // padding node
            }

            public int consume(MultiNoiseUtil.SearchTree.TreeNode<RegistryEntry<Biome>> node, int depth) {
                Objects.requireNonNull(node, "node cannot be null");
                if (depth > this.treeDepth) {
                    this.treeDepth = depth;
                }
                SerializedTreeNode serializedNode = new SerializedTreeNode();
                MultiNoiseUtil.ParameterRange[] parameters = ((IMultiNoiseUtilSearchTreeTreeNode) node).getParameters();
                Assertions.assertTrue(parameters.length == 7);
                for (int i = 0; i < 7; i++) {
                    serializedNode.maxs[i] = (short) parameters[i].max();
                    serializedNode.mins[i] = (short) parameters[i].min();
                    Assertions.assertTrue((long) serializedNode.maxs[i] == parameters[i].max(), "max value out of range: " + parameters[i].max() + " for parameter " + i);
                    Assertions.assertTrue((long) serializedNode.mins[i] == parameters[i].min(), "min value out of range: " + parameters[i].min() + " for parameter " + i);
                }
                int index = this.nodes.size();
                this.nodes.add(serializedNode);
                if (node instanceof MultiNoiseUtil.SearchTree.TreeBranchNode<RegistryEntry<Biome>> branchNode) {
                    serializedNode.isBranch = true;
                    SerializedTreeNode childrenOffsetNode = new SerializedTreeNode();
                    childrenOffsetNode.isBranch = true;
                    childrenOffsetNode.isChildrenOffsets = true;
                    Arrays.fill(childrenOffsetNode.childrenOffsets, 0);
                    this.nodes.add(childrenOffsetNode);
                    MultiNoiseUtil.SearchTree.TreeNode<RegistryEntry<Biome>>[] subTree = ((IMultiNoiseUtilSearchTreeTreeBranchNode<RegistryEntry<Biome>>) (Object) branchNode).getSubTree();
                    Assertions.assertTrue(subTree.length <= 7);
                    for (int i = 0; i < subTree.length; i++) {
                        MultiNoiseUtil.SearchTree.TreeNode<RegistryEntry<Biome>> child = subTree[i];
                        if (child != null) {
                            childrenOffsetNode.childrenOffsets[i] = consume(child, depth + 1);
                        } else {
                            childrenOffsetNode.childrenOffsets[i] = 0; // 0 means no child
                        }
                    }
                } else if (node instanceof MultiNoiseUtil.SearchTree.TreeLeafNode<RegistryEntry<Biome>> leafNode) {
                    int biomeId = this.biomeIdMap.computeIfAbsent(((IMultiNoiseUtilSearchTreeTreeLeafNode<RegistryEntry<Biome>>) (Object) leafNode).getValue(), _ -> this.biomeIdMap.size());
                    serializedNode.isBranch = false;
                    serializedNode.biomeId = biomeId;
                }
                return index;
            }

            public void validate() {
                Iterator<SerializedTreeNode> iterator = this.nodes.iterator();
                iterator.next(); // skip padding node
                while (iterator.hasNext()) {
                    SerializedTreeNode node = iterator.next();
                    if (!node.isBranch && node.isChildrenOffsets) {
                        throw new IllegalStateException("Leaf node cannot have children offsets");
                    }
                    if (node.isBranch) {
                        SerializedTreeNode childrenOffsetsNode = iterator.next();
                        if (!childrenOffsetsNode.isBranch || !childrenOffsetsNode.isChildrenOffsets) {
                            throw new IllegalStateException("Branch node must have children offsets in the next node");
                        }
                        if (!childrenOffsetsNode.isBranch && childrenOffsetsNode.isChildrenOffsets) {
                            throw new IllegalStateException("Leaf node cannot have children offsets");
                        }
                    }
                }
            }

            static class SerializedTreeNode {
                public boolean isBranch;
                public boolean isChildrenOffsets = false;
                public int biomeId;
                public short[] maxs = new short[7];
                public short[] mins = new short[7];
                public int[] childrenOffsets = new int[7];
            }

        }

        TreeFlattener treeFlattener = new TreeFlattener();
        treeFlattener.consume(((IMultiNoiseUtilSearchTree<RegistryEntry<Biome>>) (Object) searchTree).getFirstNode(), 1);
        treeFlattener.validate();

        RegistryEntry<Biome>[] biomes = new RegistryEntry[treeFlattener.biomeIdMap.size()];

        for (ObjectBidirectionalIterator<Object2IntMap.Entry<RegistryEntry<Biome>>> iterator = treeFlattener.biomeIdMap.object2IntEntrySet().fastIterator(); iterator.hasNext(); ) {
            Object2IntMap.Entry<RegistryEntry<Biome>> entry = iterator.next();
            if (biomes[entry.getIntValue()] != null) {
                throw new IllegalStateException("Duplicate biome ID found: " + entry.getIntValue() + " for biome " + entry.getKey());
            } else {
                biomes[entry.getIntValue()] = entry.getKey();
            }
        }


        MemorySegment segment = arena.allocate(treeFlattener.nodes.size() * biome_search_tree_node.byteSize(), 64);
        List<TreeFlattener.SerializedTreeNode> nodes = treeFlattener.nodes;
        for (int i = 0, nodesSize = nodes.size(); i < nodesSize; i++) {
            TreeFlattener.SerializedTreeNode node = nodes.get(i);
            MemorySegment slice = segment.asSlice(i * biome_search_tree_node.byteSize(), biome_search_tree_node.byteSize());
            biome_search_tree_node$state.set(slice, 0L, (node.isBranch ? 0x80000000 : 0) | (node.isChildrenOffsets ? 0x40000000 : 0) | node.biomeId);
            if (node.isChildrenOffsets) {
                MemorySegment childrenOffsets = biome_search_tree_node$branch_children$children_offset(slice);
                for (int j = 0; j < 7; j++) {
                    childrenOffsets.set(ValueLayout.JAVA_INT, j * ValueLayout.JAVA_INT.byteSize(), node.childrenOffsets[j]);
                }
            } else {
                MemorySegment maxs = biome_search_tree_node$node_minmaxs$maxs(slice);
                MemorySegment mins = biome_search_tree_node$node_minmaxs$mins(slice);
                for (int j = 0; j < 7; j++) {
                    maxs.set(ValueLayout.JAVA_SHORT, j * ValueLayout.JAVA_SHORT.byteSize(), node.maxs[j]);
                    mins.set(ValueLayout.JAVA_SHORT, j * ValueLayout.JAVA_SHORT.byteSize(), node.mins[j]);
                }
            }
        }

        return new NativeBiomeSearchTree(segment, biomes, treeFlattener.nodes.size(), treeFlattener.treeDepth);
    }

    public record NativeBiomeSearchTree(MemorySegment segment, RegistryEntry<Biome>[] biomes, int node_c, int tree_depth) {
    }

    public static final MethodHandle c2me_natives_fnlGetNoise3D = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_FLOAT,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE
            ),
            Linker.Option.critical(false)
    );

    public static final MethodHandle c2me_natives_fnlGetNoise3D_ptr = NativeLoader.linker.downcallHandle(
            FunctionDescriptor.of(
                    ValueLayout.JAVA_FLOAT,
                    ValueLayout.JAVA_LONG,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_DOUBLE
            ),
            Linker.Option.critical(false)
    );


    public static final StructLayout fnl_state = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("seed"),
            ValueLayout.JAVA_FLOAT.withName("frequency"),
            ValueLayout.JAVA_INT.withName("noise_type"),
            ValueLayout.JAVA_INT.withName("rotation_type_3d"),
            ValueLayout.JAVA_INT.withName("fractal_type"),
            ValueLayout.JAVA_INT.withName("octaves"),
            ValueLayout.JAVA_FLOAT.withName("lacunarity"),
            ValueLayout.JAVA_FLOAT.withName("gain"),
            ValueLayout.JAVA_FLOAT.withName("weighted_strength"),
            ValueLayout.JAVA_FLOAT.withName("ping_pong_strength"),
            ValueLayout.JAVA_INT.withName("cellular_distance_func"),
            ValueLayout.JAVA_INT.withName("cellular_return_type"),
            ValueLayout.JAVA_FLOAT.withName("cellular_jitter_mod"),
            ValueLayout.JAVA_INT.withName("domain_warp_type"),
            ValueLayout.JAVA_FLOAT.withName("domain_warp_amp")
    ).withName("fnl_state");

    public static final VarHandle fnl_state$seed = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("seed"));
    public static final VarHandle fnl_state$frequency = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("frequency"));
    public static final VarHandle fnl_state$noise_type = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("noise_type"));
    public static final VarHandle fnl_state$rotation_type_3d = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("rotation_type_3d"));
    public static final VarHandle fnl_state$fractal_type = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("fractal_type"));
    public static final VarHandle fnl_state$octaves = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("octaves"));
    public static final VarHandle fnl_state$lacunarity = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("lacunarity"));
    public static final VarHandle fnl_state$gain = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("gain"));
    public static final VarHandle fnl_state$weighted_strength = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("weighted_strength"));
    public static final VarHandle fnl_state$ping_pong_strength = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("ping_pong_strength"));
    public static final VarHandle fnl_state$cellular_distance_func = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("cellular_distance_func"));
    public static final VarHandle fnl_state$cellular_return_type = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("cellular_return_type"));
    public static final VarHandle fnl_state$cellular_jitter_mod = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("cellular_jitter_mod"));
    public static final VarHandle fnl_state$domain_warp_type = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("domain_warp_type"));
    public static final VarHandle fnl_state$domain_warp_amp = fnl_state.varHandle(MemoryLayout.PathElement.groupElement("domain_warp_amp"));

    public static MemorySegment fnl_state$create(Arena arena, FNLBindings.FNLState state) {
        final MemorySegment data = arena.allocate(fnl_state.byteSize());
        fnl_state$seed.set(data, 0L, state.seed());
        fnl_state$frequency.set(data, 0L, state.frequency());
        fnl_state$noise_type.set(data, 0L, state.noise_type());
        fnl_state$rotation_type_3d.set(data, 0L, state.rotation_type_3d());
        fnl_state$fractal_type.set(data, 0L, state.fractal_type());
        fnl_state$octaves.set(data, 0L, state.octaves());
        fnl_state$lacunarity.set(data, 0L, state.lacunarity());
        fnl_state$gain.set(data, 0L, state.gain());
        fnl_state$weighted_strength.set(data, 0L, state.weighted_strength());
        fnl_state$ping_pong_strength.set(data, 0L, state.ping_pong_strength());
        fnl_state$cellular_distance_func.set(data, 0L, state.cellular_distance_func());
        fnl_state$cellular_return_type.set(data, 0L, state.cellular_return_type());
        fnl_state$cellular_jitter_mod.set(data, 0L, state.cellular_jitter_mod());
        fnl_state$domain_warp_type.set(data, 0L, state.domain_warp_type());
        fnl_state$domain_warp_amp.set(data, 0L, state.domain_warp_amp());
        return data;
    }

}
