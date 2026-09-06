package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.opts.dfc.common.ast.misc.Multi2SingleNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;

public class Multi2SingleNodeSpirVEmitter implements SpirVEmitter<Multi2SingleNode> {

    public static final Multi2SingleNodeSpirVEmitter INSTANCE = new Multi2SingleNodeSpirVEmitter();

    private Multi2SingleNodeSpirVEmitter() {
    }

    @Override
    public int doSpirVGen(Multi2SingleNode node, SpirVGenFunctionContext context) {
        ValuesMethodDef next = context.newVar(node.next);
        return context.getDelegateVar(next, next.returnType());
    }

}
