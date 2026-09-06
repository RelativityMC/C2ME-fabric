package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.SpirVEmitterUtil;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.noise.GenericShiftedNoiseNode;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenContext;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;

public class GenericShiftedNoiseNodeSpirVEmitter implements SpirVEmitter<GenericShiftedNoiseNode> {

    public static final GenericShiftedNoiseNodeSpirVEmitter INSTANCE = new GenericShiftedNoiseNodeSpirVEmitter();

    private GenericShiftedNoiseNodeSpirVEmitter() {
    }

    @Override
    public int doSpirVGen(GenericShiftedNoiseNode node, SpirVGenFunctionContext context) {
        SpirVGenContext global = context.getGlobalContext();
        if (node.noise.noise() == null) {
            return global.constF64(0.0);
        }

        int inputX = context.getDelegateVar(context.newVarF64(node.inputX));
        int inputY = context.getDelegateVar(context.newVarF64(node.inputY));
        int inputZ = context.getDelegateVar(context.newVarF64(node.inputZ));

        int data = SpirVEmitterUtil.constDataAddress(context, global.allocGlobalConstDataObject(node.noise.noise()));

        return context.op(SpirV.OP_FUNCTION_CALL, global.typeFloat(64), global.preludeFunctionId("math_noise_perlin_double_octave_sample_global_noinline"), data, inputX, inputY, inputZ);
    }

}
