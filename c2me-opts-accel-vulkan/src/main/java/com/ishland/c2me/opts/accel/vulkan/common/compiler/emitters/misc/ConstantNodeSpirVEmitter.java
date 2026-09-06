package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;

public class ConstantNodeSpirVEmitter implements SpirVEmitter<ConstantNode> {

    public static final ConstantNodeSpirVEmitter INSTANCE = new ConstantNodeSpirVEmitter();

    private ConstantNodeSpirVEmitter() {
    }

    @Override
    public int doSpirVGen(ConstantNode node, SpirVGenFunctionContext context) {
        return context.getGlobalContext().constF64(node.getValue());
    }

}
