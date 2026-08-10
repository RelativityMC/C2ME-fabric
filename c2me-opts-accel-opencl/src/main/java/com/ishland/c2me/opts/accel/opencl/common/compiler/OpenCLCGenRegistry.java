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

package com.ishland.c2me.opts.accel.opencl.common.compiler;

import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.BinaryNodeOpenCLCEmitters;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.UnaryNodeOpenCLCEmitters;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.conversion.ToF32NodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.conversion.ToF64NodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.BeardifierNodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.CacheLikeNodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.ConstantF32NodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.ConstantNodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.CoordinateNodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.DFTWeirdScaledSamplerNodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.EndIslandsNodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.FindTopSurfaceNodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.GenericShiftedNoiseNodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.InterpolatedNoiseSamplerNodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.Multi2SingleNodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.RangeChoiceNodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.RootNodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.SplineNormalNodeOpenCLCEmitter;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.YClampedGradientNodeOpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF32Node;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF64Node;
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
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineAstNode;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineNormalNode;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenData;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenFunctionContext;

public class OpenCLCGenRegistry {

    static {
        BinaryNodeOpenCLCEmitters.register(OpenCLCGenData.REGISTRY);
        UnaryNodeOpenCLCEmitters.register(OpenCLCGenData.REGISTRY);

        OpenCLCGenData.REGISTRY.registerExactMatch(ToF32Node.class, ToF32NodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(ToF64Node.class, ToF64NodeOpenCLCEmitter.INSTANCE);

        OpenCLCGenData.REGISTRY.registerExactMatch(BeardifierNode.class, BeardifierNodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(CacheLikeNode.class, CacheLikeNodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(ConstantNode.class, ConstantNodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(ConstantF32Node.class, ConstantF32NodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(CoordinateNode.class, CoordinateNodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(DFTWeirdScaledSamplerNode.class, DFTWeirdScaledSamplerNodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(EndIslandsNode.class, EndIslandsNodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(FindTopSurfaceNode.class, FindTopSurfaceNodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(GenericShiftedNoiseNode.class, GenericShiftedNoiseNodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(InterpolatedNoiseSamplerNode.class, InterpolatedNoiseSamplerNodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(RangeChoiceNode.class, RangeChoiceNodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(RootNode.class, RootNodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(Multi2SingleNode.class, Multi2SingleNodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(SplineNormalNode.class, SplineNormalNodeOpenCLCEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(YClampedGradientNode.class, YClampedGradientNodeOpenCLCEmitter.INSTANCE);

        OpenCLCGenData.REGISTRY.registerExactMatch(DelegateNode.class, (OpenCLCEmitter<DelegateNode>) (node, context, storeTo) -> {
            throw new UnsupportedOperationException(String.format("Unsupported density function type: %s", node.getDelegate().getClass()));
        });
    }

    public static <T extends AstNode> String doCLGen(T node, OpenCLCGenFunctionContext context, String storeTo) {
        OpenCLCEmitter<T> emitter = (OpenCLCEmitter<T>) OpenCLCGenData.REGISTRY.get(node.getClass());
        return emitter.doCLGen(node, context, storeTo);
    }

}
