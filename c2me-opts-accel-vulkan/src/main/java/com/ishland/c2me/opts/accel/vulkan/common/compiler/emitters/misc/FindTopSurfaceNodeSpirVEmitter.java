package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.misc.FindTopSurfaceNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenContext;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;

public class FindTopSurfaceNodeSpirVEmitter implements SpirVEmitter<FindTopSurfaceNode> {

    public static final FindTopSurfaceNodeSpirVEmitter INSTANCE = new FindTopSurfaceNodeSpirVEmitter();

    private static final int MASK_ENABLE_FLAT_CACHE = 1;

    private FindTopSurfaceNodeSpirVEmitter() {
    }

    private static int contextAt(SpirVGenFunctionContext context, SpirVGenContext global, int y, int u32) {
        int ctxType = global.typeSampleCtx();
        int composite = context.op(SpirV.OP_COMPOSITE_CONSTRUCT, ctxType,
                context.paramConstData(),
                context.paramRwData(),
                context.paramX(),
                y,
                context.paramZ(),
                global.constU32(MASK_ENABLE_FLAT_CACHE));
        int local = context.newLocal(ctxType);
        context.store(local, composite);
        return local;
    }

    @Override
    public int doSpirVGen(FindTopSurfaceNode node, SpirVGenFunctionContext context) {
        SpirVGenContext global = context.getGlobalContext();
        int f64 = global.typeFloat(64);
        int i32 = global.typeInt(32, true);
        int u32 = global.typeInt(32, false);
        int bool = global.typeBool();

        ValuesMethodDefF64 densityMethod = global.newDispatcherF64(node.density, global.nextMethodName());
        int upperBound = context.getDelegateVar(context.newVarF64(node.upperBound));
        int lowerBound = context.getDelegateVar(context.newVarF64(node.lowerBound));

        int cellHeightF = global.constF64(node.cellHeight);
        int scaled = context.op(SpirV.OP_F_DIV, f64, upperBound, cellHeightF);
        int floored = context.extInst(global.extInstImport(SpirV.Glsl450.NAME), SpirV.Glsl450.FLOOR, f64, scaled);
        int topCellBlockY = context.op(SpirV.OP_I_MUL, i32,
                context.op(SpirV.OP_CONVERT_F_TO_S, i32, floored), global.constI32(node.cellHeight));
        int lowerBoundEval = context.op(SpirV.OP_CONVERT_F_TO_S, i32, lowerBound);

        int y = context.newLocal(i32);
        int result = context.newLocal(f64);
        context.store(y, topCellBlockY);
        context.store(result, context.op(SpirV.OP_CONVERT_S_TO_F, f64, lowerBoundEval));

        int header = context.newLabel();
        int condition = context.newLabel();
        int body = context.newLabel();
        int continueTarget = context.newLabel();
        int merge = context.newLabel();

        context.branch(header);

        context.label(header);
        context.loopMerge(merge, continueTarget);
        context.branch(condition);

        context.label(condition);
        int keepGoing = context.op(SpirV.OP_S_GREATER_THAN, bool, context.load(i32, y), lowerBoundEval);
        context.branchConditional(keepGoing, body, merge);

        context.label(body);
        int currentY = context.load(i32, y);
        int density = densityMethod.isConst() ? global.constF64(densityMethod.constValue()) : context.op(SpirV.OP_FUNCTION_CALL, f64, global.functionId(densityMethod), contextAt(context, global, currentY, u32));
        int positive = context.op(SpirV.OP_F_ORD_GREATER_THAN, bool, density, global.constF64(0.0));

        int found = context.newLabel();
        int notFound = context.newLabel();
        context.selectionMerge(notFound);
        context.branchConditional(positive, found, notFound);

        context.label(found);
        context.store(result, context.op(SpirV.OP_CONVERT_S_TO_F, f64, currentY));
        context.branch(merge);

        context.label(notFound);
        context.branch(continueTarget);

        context.label(continueTarget);
        context.store(y, context.op(SpirV.OP_I_SUB, i32, context.load(i32, y), global.constI32(node.cellHeight)));
        context.branch(header);

        context.label(merge);
        return context.load(f64, result);
    }

}
