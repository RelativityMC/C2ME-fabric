package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantF32Node;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;

public class ConstantF32NodeSpirVEmitter implements SpirVEmitter<ConstantF32Node> {

    public static final ConstantF32NodeSpirVEmitter INSTANCE = new ConstantF32NodeSpirVEmitter();

    private ConstantF32NodeSpirVEmitter() {
    }

    @Override
    public int doSpirVGen(ConstantF32Node node, SpirVGenFunctionContext context) {
        return context.getGlobalContext().constF32(node.getValue());
    }

}
