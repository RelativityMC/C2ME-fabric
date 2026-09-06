package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.opts.accel.vulkan.common.Config;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.SpirVEmitterUtil;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.IntervalSelectNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;
import com.ishland.c2me.opts.dfc.common.util.TreeUtils;
import com.ishland.flowsched.util.Assertions;

public class IntervalSelectNodeSpirVEmitter implements SpirVEmitter<IntervalSelectNode> {

    public static final IntervalSelectNodeSpirVEmitter INSTANCE = new IntervalSelectNodeSpirVEmitter();

    private IntervalSelectNodeSpirVEmitter() {
    }

    /**
     * Nested selection constructs mirroring the OpenCL backend's nested if/else.
     */
    private static void genBinarySearch(double[] thresholds, Object[] delegates, SpirVGenFunctionContext context,
                                        int input, int result, int fromIndex, int toIndex) {
        Assertions.assertTrue(fromIndex < toIndex);
        int mid = (fromIndex + toIndex - 1) >>> 1;

        int condition = context.op(SpirV.OP_F_ORD_LESS_THAN, context.getGlobalContext().typeBool(), input,
                context.getGlobalContext().constF64(thresholds[mid]));

        SpirVEmitterUtil.ifElse(context, condition,
                () -> {
                    if (fromIndex == mid) {
                        emitCall(delegates, context, result, fromIndex);
                    } else {
                        genBinarySearch(thresholds, delegates, context, input, result, fromIndex, mid);
                    }
                },
                () -> {
                    if (mid + 1 == toIndex) {
                        emitCall(delegates, context, result, toIndex);
                    } else {
                        genBinarySearch(thresholds, delegates, context, input, result, mid + 1, toIndex);
                    }
                });
    }

    private static void emitCall(Object[] delegates, SpirVGenFunctionContext context, int result, int idx) {
        switch (delegates[idx]) {
            case ValuesMethodDefF64 def -> context.store(result, context.getDelegateVar(def));
            case AstNode node -> {
                SpirVGenFunctionContext arm = context.fork();
                context.store(result, arm.getDelegateVar(arm.newVarF64(node)));
                // Emitted into one arm only; a second reference would not be dominated by it.
                delegates[idx] = null;
            }
            case null -> throw new IllegalStateException("Delegate " + idx + " already emitted");
            default -> throw new IllegalArgumentException(
                    "Invalid delegate type: " + delegates[idx].getClass().getName());
        }
    }

    @Override
    public int doSpirVGen(IntervalSelectNode node, SpirVGenFunctionContext context) {
        int f64 = context.getGlobalContext().typeFloat(64);

        int input = context.getDelegateVar(context.newVarF64(node.input));

        AstNode[] functions = node.functions;
        Object[] delegates = new Object[functions.length];
        for (int i = 0; i < functions.length; i++) {
            AstNode function = functions[i];
            if (Config.preserveAllControlFlows) {
                delegates[i] = context.getGlobalContext().newMethodF64(function, context.getVariant());
            } else if (TreeUtils.hasNonTrivialChildrenUntilBranch(function)) {
                // Deferred: emitted inside its own branch arm so it is only evaluated when taken.
                delegates[i] = function;
            } else {
                delegates[i] = context.newVarF64(function);
            }
        }

        int result = context.newLocal(f64);
        genBinarySearch(node.thresholds, delegates, context, input, result, 0, node.thresholds.length);
        return context.load(f64, result);
    }

}
