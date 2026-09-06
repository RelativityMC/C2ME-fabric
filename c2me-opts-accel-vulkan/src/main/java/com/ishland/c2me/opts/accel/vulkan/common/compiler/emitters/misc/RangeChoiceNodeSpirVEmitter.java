package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.opts.accel.vulkan.common.Config;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.SpirVEmitterUtil;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceNode;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;
import com.ishland.c2me.opts.dfc.common.util.TreeUtils;

public class RangeChoiceNodeSpirVEmitter implements SpirVEmitter<RangeChoiceNode> {

    public static final RangeChoiceNodeSpirVEmitter INSTANCE = new RangeChoiceNodeSpirVEmitter();

    private RangeChoiceNodeSpirVEmitter() {
    }

    private static int emitArm(SpirVGenFunctionContext context, AstNode node) {
        if (Config.preserveAllControlFlows) {
            return context.getDelegateVar(
                    context.getGlobalContext().newMethodF64(node, context.getVariant()));
        }

        SpirVGenFunctionContext arm = TreeUtils.hasNonTrivialChildrenUntilBranchIgnoringMul(node) ? context.fork() : context;
        return arm.getDelegateVar(arm.newVarF64(node));
    }

    @Override
    public int doSpirVGen(RangeChoiceNode node, SpirVGenFunctionContext context) {
        int f64 = context.getGlobalContext().typeFloat(64);
        int bool = context.getGlobalContext().typeBool();

        if (!Config.preserveAllControlFlows) {
            for (AstNode subtree : TreeUtils.findLargestCommonSubtrees(node.whenInRange, node.whenOutOfRange)) {
                context.newVar(subtree);
            }
        }

        int input = context.getDelegateVar(context.newVarF64(node.input));
        int atLeastMin = context.op(SpirV.OP_F_ORD_GREATER_THAN_EQUAL, bool, input,
                context.getGlobalContext().constF64(node.minInclusive));
        int belowMax = context.op(SpirV.OP_F_ORD_LESS_THAN, bool, input,
                context.getGlobalContext().constF64(node.maxExclusive));
        int inRange = context.op(SpirV.OP_LOGICAL_AND, bool, atLeastMin, belowMax);

        int result = context.newLocal(f64);
        SpirVEmitterUtil.ifElse(context, inRange,
                () -> context.store(result, emitArm(context, node.whenInRange)),
                () -> context.store(result, emitArm(context, node.whenOutOfRange)));
        return context.load(f64, result);
    }

}
