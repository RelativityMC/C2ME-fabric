package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateNode;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;

public class CoordinateNodeSpirVEmitter implements SpirVEmitter<CoordinateNode> {

    public static final CoordinateNodeSpirVEmitter INSTANCE = new CoordinateNodeSpirVEmitter();

    private CoordinateNodeSpirVEmitter() {
    }

    @Override
    public int doSpirVGen(CoordinateNode node, SpirVGenFunctionContext context) {
        int coordinate = switch (node.axis) {
            case X -> context.paramX();
            case Y -> context.paramY();
            case Z -> context.paramZ();
        };
        // ctx members are signed 32-bit; the density function graph is double.
        return context.op(SpirV.OP_CONVERT_S_TO_F, context.getGlobalContext().typeFloat(64), coordinate);
    }

}
