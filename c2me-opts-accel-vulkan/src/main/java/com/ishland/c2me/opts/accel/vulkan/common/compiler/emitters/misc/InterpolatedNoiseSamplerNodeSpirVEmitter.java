package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.SpirVEmitterUtil;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.misc.InterpolatedNoiseSamplerNode;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenContext;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;

public class InterpolatedNoiseSamplerNodeSpirVEmitter implements SpirVEmitter<InterpolatedNoiseSamplerNode> {

    public static final InterpolatedNoiseSamplerNodeSpirVEmitter INSTANCE = new InterpolatedNoiseSamplerNodeSpirVEmitter();

    private InterpolatedNoiseSamplerNodeSpirVEmitter() {
    }

    @Override
    public int doSpirVGen(InterpolatedNoiseSamplerNode node, SpirVGenFunctionContext context) {
        SpirVGenContext global = context.getGlobalContext();
        int f64 = global.typeFloat(64);

        int data = SpirVEmitterUtil.constDataAddress(context, global.allocGlobalConstDataObject(node.sampler));

        int x = context.op(SpirV.OP_CONVERT_S_TO_F, f64, context.paramX());
        int y = context.op(SpirV.OP_CONVERT_S_TO_F, f64, context.paramY());
        int z = context.op(SpirV.OP_CONVERT_S_TO_F, f64, context.paramZ());

        return context.op(SpirV.OP_FUNCTION_CALL, f64, global.preludeFunctionId("math_noise_perlin_interpolated_sample_global_noinline"), data, x, y, z);
    }

}
