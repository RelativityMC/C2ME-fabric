package com.ishland.c2me.opts.accel.vulkan.common.compiler;

import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.BinaryNodeSpirVEmitters;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.UnaryNodeSpirVEmitters;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.conversion.ToF32NodeSpirVEmitter;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.conversion.ToF64NodeSpirVEmitter;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc.*;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF32Node;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.*;
import com.ishland.c2me.opts.dfc.common.ast.noise.GenericShiftedNoiseNode;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineNormalNode;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenRegistry;

/**
 * Populates {@link SpirVGenRegistry#REGISTRY}. The counterpart of the OpenCL backend's
 * {@code OpenCLCGenRegistry}; the registry itself lives in {@code c2me-opts-dfc} alongside the
 * emitter interface, as {@code OpenCLCGenData} does.
 */
public class SpirVEmitterRegistry {

    static {
        BinaryNodeSpirVEmitters.register(SpirVGenRegistry.REGISTRY);
        UnaryNodeSpirVEmitters.register(SpirVGenRegistry.REGISTRY);

        SpirVGenRegistry.REGISTRY.registerExactMatch(ToF32Node.class, ToF32NodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(ToF64Node.class, ToF64NodeSpirVEmitter.INSTANCE);

        SpirVGenRegistry.REGISTRY.registerExactMatch(BeardifierNode.class, BeardifierNodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(CacheLikeNode.class, CacheLikeNodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(ConstantNode.class, ConstantNodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(ConstantF32Node.class, ConstantF32NodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(CoordinateNode.class, CoordinateNodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(EndIslandsNode.class, EndIslandsNodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(FindTopSurfaceNode.class, FindTopSurfaceNodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(GenericShiftedNoiseNode.class, GenericShiftedNoiseNodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(InterpolatedNoiseSamplerNode.class, InterpolatedNoiseSamplerNodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(IntervalSelectNode.class, IntervalSelectNodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(Multi2SingleNode.class, Multi2SingleNodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(RangeChoiceNode.class, RangeChoiceNodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(RootNode.class, RootNodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(SplineNormalNode.class, SplineNormalNodeSpirVEmitter.INSTANCE);
        SpirVGenRegistry.REGISTRY.registerExactMatch(YClampedGradientNode.class, YClampedGradientNodeSpirVEmitter.INSTANCE);

        // DelegateNode is the catch-all: reaching it means the frontend produced a density
        // function this backend has no lowering for.
        SpirVGenRegistry.REGISTRY.registerExactMatch(DelegateNode.class, (SpirVEmitter<DelegateNode>) (node, context) -> {
            throw new UnsupportedOperationException(
                    String.format("Unsupported density function type: %s", node.getDelegate().getClass()));
        });
    }

    /**
     * Forces the registrations above; the registry freezes on first lookup.
     */
    public static void init() {
    }

    public static <T extends AstNode> int doSpirVGen(T node, SpirVGenFunctionContext context) {
        SpirVEmitter<T> emitter = (SpirVEmitter<T>) SpirVGenRegistry.REGISTRY.get(node.getClass());
        return emitter.doSpirVGen(node, context);
    }

}
