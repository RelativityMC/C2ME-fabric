package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.opts.dfc.common.ast.misc.RootNode;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;

public class RootNodeSpirVEmitter implements SpirVEmitter<RootNode> {

    public static final RootNodeSpirVEmitter INSTANCE = new RootNodeSpirVEmitter();

    private RootNodeSpirVEmitter() {
    }

    @Override
    public int doSpirVGen(RootNode node, SpirVGenFunctionContext context) {
        return context.getDelegateVar(context.newVarF64(node.next));
    }

}
