package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.conversion;

import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF64Node;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;

public class ToF64NodeSpirVEmitter implements SpirVEmitter<ToF64Node> {

    public static final ToF64NodeSpirVEmitter INSTANCE = new ToF64NodeSpirVEmitter();

    private ToF64NodeSpirVEmitter() {
    }

    @Override
    public int doSpirVGen(ToF64Node node, SpirVGenFunctionContext context) {
        ValuesMethodDef next = context.newVar(node.next);
        int value = context.getDelegateVar(next, next.returnType());
        if (next.returnType() == AstNode.ReturnType.F64) return value;
        return context.op(SpirV.OP_F_CONVERT, context.getGlobalContext().typeFloat(64), value);
    }

}
