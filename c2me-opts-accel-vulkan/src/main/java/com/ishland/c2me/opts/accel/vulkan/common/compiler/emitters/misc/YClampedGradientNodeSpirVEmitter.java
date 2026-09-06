package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.misc.YClampedGradientNode;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;

public class YClampedGradientNodeSpirVEmitter implements SpirVEmitter<YClampedGradientNode> {

    public static final YClampedGradientNodeSpirVEmitter INSTANCE = new YClampedGradientNodeSpirVEmitter();

    private YClampedGradientNodeSpirVEmitter() {
    }

    @Override
    public int doSpirVGen(YClampedGradientNode node, SpirVGenFunctionContext context) {
        int f64 = context.getGlobalContext().typeFloat(64);
        int y = context.op(SpirV.OP_CONVERT_S_TO_F, f64, context.paramY());

        return context.op(SpirV.OP_FUNCTION_CALL, f64,
                context.getGlobalContext().preludeFunctionId("math_clampedMap"),
                y,
                context.getGlobalContext().constF64(node.fromY),
                context.getGlobalContext().constF64(node.toY),
                context.getGlobalContext().constF64(node.fromValue),
                context.getGlobalContext().constF64(node.toValue));
    }

}
