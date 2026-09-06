package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.conversion;

import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF32Node;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;

public class ToF32NodeSpirVEmitter implements SpirVEmitter<ToF32Node> {

    public static final ToF32NodeSpirVEmitter INSTANCE = new ToF32NodeSpirVEmitter();

    private ToF32NodeSpirVEmitter() {
    }

    @Override
    public int doSpirVGen(ToF32Node node, SpirVGenFunctionContext context) {
        ValuesMethodDef next = context.newVar(node.next);
        int value = context.getDelegateVar(next, next.returnType());
        if (next.returnType() == AstNode.ReturnType.F32) return value;
        return context.op(SpirV.OP_F_CONVERT, context.getGlobalContext().typeFloat(32), value);
    }

}
