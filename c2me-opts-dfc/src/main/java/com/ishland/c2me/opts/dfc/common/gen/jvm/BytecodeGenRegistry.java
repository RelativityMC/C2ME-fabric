/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ishland.c2me.opts.dfc.common.gen.jvm;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF32Node;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF64Node;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.GenericFastNoiseNode;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.MixNode;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.SelectNode;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.RepositionNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.BeardifierNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.EndIslandsNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.FindTopSurfaceNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.InterpolatedNoiseSamplerNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.Multi2SingleNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RootNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.YClampedGradientNode;
import com.ishland.c2me.opts.dfc.common.ast.noise.DFTWeirdScaledSamplerNode;
import com.ishland.c2me.opts.dfc.common.ast.noise.GenericShiftedNoiseNode;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineNormalNode;
import com.ishland.c2me.opts.dfc.common.gen.CodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.BinaryNodeBytecodeEmitters;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.UnaryNodeBytecodeEmitters;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.conversion.ToF32NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.conversion.ToF64NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.integration.lithostitched.misc.GenericFastNoiseNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.integration.lithostitched.misc.MixNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.integration.lithostitched.misc.SelectNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.integration.lithostitched.misc.RepositionNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.CacheLikeNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.ConstantF32NodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.ConstantNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.CoordinateNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.DFTWeirdScaledSamplerNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.DelegateNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.FindTopSurfaceNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.GenericShiftedNoiseNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.Multi2SingleNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.RangeChoiceNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.RootNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.SplineNormalNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.YClampedGradientNodeBytecodeEmitter;
import org.objectweb.asm.commons.InstructionAdapter;

public class BytecodeGenRegistry {

    public static final CodeGenRegistry<BytecodeEmitter<? extends AstNode>> REGISTRY = new CodeGenRegistry<>();

    static {
        BinaryNodeBytecodeEmitters.register(REGISTRY);
        UnaryNodeBytecodeEmitters.register(REGISTRY);

        REGISTRY.registerExactMatch(ToF32Node.class, ToF32NodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(ToF64Node.class, ToF64NodeBytecodeEmitter.INSTANCE);

        REGISTRY.registerExactMatch(CacheLikeNode.class, CacheLikeNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(ConstantNode.class, ConstantNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(ConstantF32Node.class, ConstantF32NodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(CoordinateNode.class, CoordinateNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(FindTopSurfaceNode.class, FindTopSurfaceNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(GenericShiftedNoiseNode.class, GenericShiftedNoiseNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(RangeChoiceNode.class, RangeChoiceNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(RootNode.class, RootNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(YClampedGradientNode.class, YClampedGradientNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(DFTWeirdScaledSamplerNode.class, DFTWeirdScaledSamplerNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(Multi2SingleNode.class, Multi2SingleNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(SplineNormalNode.class, SplineNormalNodeBytecodeEmitter.INSTANCE);
//        REGISTRY.registerExactMatch(SplineAstNode.class, SplineAstNodeBytecodeEmitter.INSTANCE);

        REGISTRY.registerExactMatch(DelegateNode.class, DelegateNodeBytecodeEmitter.instance());
        REGISTRY.registerExactMatch(BeardifierNode.class, DelegateNodeBytecodeEmitter.instance());
        REGISTRY.registerExactMatch(EndIslandsNode.class, DelegateNodeBytecodeEmitter.instance());
        REGISTRY.registerExactMatch(InterpolatedNoiseSamplerNode.class, DelegateNodeBytecodeEmitter.instance());

        REGISTRY.registerExactMatch(GenericFastNoiseNode.class, GenericFastNoiseNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(MixNode.class,  MixNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(SelectNode.class, SelectNodeBytecodeEmitter.INSTANCE);
        REGISTRY.registerExactMatch(RepositionNode.class, RepositionNodeBytecodeEmitter.INSTANCE);
    }

    public static <T extends AstNode> void doBytecodeGenSingle(T node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        BytecodeEmitter<T> emitter = (BytecodeEmitter<T>) REGISTRY.get(node.getClass());
        emitter.doBytecodeGenSingle(node, context, m, localVarConsumer);
    }

    public static <T extends AstNode> void doBytecodeGenMulti(T node, BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        BytecodeEmitter<T> emitter = (BytecodeEmitter<T>) REGISTRY.get(node.getClass());
        emitter.doBytecodeGenMulti(node, context, m, localVarConsumer);
    }

}
