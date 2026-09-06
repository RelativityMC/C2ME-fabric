package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.base.mixin.access.IDensityFunctionTypesEndIslands;
import com.ishland.c2me.base.mixin.access.ISimplexNoiseSampler;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.SpirVEmitterUtil;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.misc.EndIslandsNode;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenContext;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;

public class EndIslandsNodeSpirVEmitter implements SpirVEmitter<EndIslandsNode> {

    public static final EndIslandsNodeSpirVEmitter INSTANCE = new EndIslandsNodeSpirVEmitter();

    private EndIslandsNodeSpirVEmitter() {
    }

    @Override
    public int doSpirVGen(EndIslandsNode node, SpirVGenFunctionContext context) {
        SpirVGenContext global = context.getGlobalContext();
        int f64 = global.typeFloat(64);
        int i32 = global.typeInt(32, true);

        int[] permutation = ((ISimplexNoiseSampler) ((IDensityFunctionTypesEndIslands) (Object) node.endIslands)
                .getSampler()).getPermutation();
        int data = SpirVEmitterUtil.constDataAddress(context, global.allocGlobalConstDataObject(permutation));

        int eight = global.constI32(8);
        int x = context.op(SpirV.OP_S_DIV, i32, context.paramX(), eight);
        int z = context.op(SpirV.OP_S_DIV, i32, context.paramZ(), eight);

        // The helper returns float; the result is (sample - 8.0) / 128.0 in double.
        int sample = context.op(SpirV.OP_FUNCTION_CALL, global.typeFloat(32),
                global.preludeFunctionId("math_end_islands_sample_global"), data, x, z);
        int widened = context.op(SpirV.OP_F_CONVERT, f64, sample);
        int shifted = context.op(SpirV.OP_F_SUB, f64, widened, global.constF64(8.0));
        return context.op(SpirV.OP_F_DIV, f64, shifted, global.constF64(128.0));
    }

}
