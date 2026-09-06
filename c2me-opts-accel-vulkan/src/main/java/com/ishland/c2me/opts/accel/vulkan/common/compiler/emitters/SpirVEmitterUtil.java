package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters;

import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;

public final class SpirVEmitterUtil {

    private SpirVEmitterUtil() {
    }

    public static void ifElse(SpirVGenFunctionContext context, int condition, Runnable thenArm, Runnable elseArm) {
        int thenLabel = context.newLabel();
        int elseLabel = context.newLabel();
        int mergeLabel = context.newLabel();

        context.selectionMerge(mergeLabel);
        context.branchConditional(condition, thenLabel, elseLabel);

        context.label(thenLabel);
        thenArm.run();
        context.branch(mergeLabel);

        context.label(elseLabel);
        elseArm.run();
        context.branch(mergeLabel);

        context.label(mergeLabel);
    }

    public static int constDataAddress(SpirVGenFunctionContext context, int byteOffset) {
        return context.op(SpirV.OP_I_ADD, context.getGlobalContext().typeInt(64, false),
                context.paramConstData(), context.getGlobalContext().constU64(byteOffset));
    }

}
