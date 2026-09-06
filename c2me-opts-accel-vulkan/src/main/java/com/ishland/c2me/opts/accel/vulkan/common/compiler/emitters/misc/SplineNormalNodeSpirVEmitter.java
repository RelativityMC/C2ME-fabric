package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.opts.accel.vulkan.common.Config;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.SpirVEmitterUtil;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineNormalNode;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenContext;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.ArrayList;
import java.util.List;

public class SplineNormalNodeSpirVEmitter implements SpirVEmitter<SplineNormalNode> {

    public static final SplineNormalNodeSpirVEmitter INSTANCE = new SplineNormalNodeSpirVEmitter();

    private SplineNormalNodeSpirVEmitter() {
    }

    /**
     * Emits the value function at {@code index}, honouring the control flow preservation mode.
     */
    private static int value(SpirVGenFunctionContext context, SplineNormalNode node, int index) {
        if (Config.preserveAllControlFlows) {
            return context.getDelegateVar(
                    context.getGlobalContext().newMethodF32(node.values[index], context.getVariant()));
        }
        return context.getDelegateVar(context.newVarF32(node.values[index]));
    }

    private static int sampleOutsideRange(SpirVGenFunctionContext context, int point, int locations,
                                          int value, int derivatives, int index) {
        return context.op(SpirV.OP_FUNCTION_CALL, context.getGlobalContext().typeFloat(32),
                context.getGlobalContext().preludeFunctionId("df_spline_sampleOutsideRange"),
                point, locations, value, derivatives, index);
    }

    /**
     * The in-range case: pick the bracketing knot values with an {@code OpSwitch}, then evaluate
     * the cubic. Intervals whose two endpoint subtrees are equal share a case, as in the OpenCL
     * backend.
     */
    private static int interpolate(SpirVGenFunctionContext context, SplineNormalNode node, int point,
                                   int locations, int derivatives, int rangeForLocation) {
        SpirVGenContext global = context.getGlobalContext();
        int f32 = global.typeFloat(32);

        int loc0 = loadF32(context, locations, rangeForLocation);
        int nextIndex = context.op(SpirV.OP_I_ADD, global.typeInt(32, true), rangeForLocation, global.constI32(1));
        int loc1 = loadF32(context, locations, nextIndex);
        int locDist = context.op(SpirV.OP_F_SUB, f32, loc1, loc0);
        int k = context.op(SpirV.OP_F_DIV, f32, context.op(SpirV.OP_F_SUB, f32, point, loc0), locDist);

        int n = context.newLocal(f32);
        int o = context.newLocal(f32);

        int intervals = node.values.length - 1;
        boolean[] emitted = new boolean[intervals];
        List<IntArrayList> caseGroups = new ArrayList<>();
        for (int i = 0; i < intervals; i++) {
            if (emitted[i]) continue;
            IntArrayList group = new IntArrayList();
            group.add(i);
            emitted[i] = true;
            for (int j = i + 1; j < intervals; j++) {
                if (node.values[i].equals(node.values[j]) && node.values[i + 1].equals(node.values[j + 1])) {
                    group.add(j);
                    emitted[j] = true;
                }
            }
            caseGroups.add(group);
        }

        int switchMerge = context.newLabel();
        int defaultLabel = context.newLabel();
        IntArrayList literals = new IntArrayList();
        IntArrayList labels = new IntArrayList();
        int[] groupLabels = new int[caseGroups.size()];
        for (int g = 0; g < caseGroups.size(); g++) {
            groupLabels[g] = context.newLabel();
            for (int literal : caseGroups.get(g)) {
                literals.add(literal);
                labels.add(groupLabels[g]);
            }
        }

        context.selectionMerge(switchMerge);
        context.switchOn(rangeForLocation, defaultLabel, literals.toIntArray(), labels.toIntArray());

        for (int g = 0; g < caseGroups.size(); g++) {
            int first = caseGroups.get(g).getInt(0);
            context.label(groupLabels[g]);
            int lower = value(context, node, first);
            int upper = node.values[first].equals(node.values[first + 1]) ? lower : value(context, node, first + 1);
            context.store(n, lower);
            context.store(o, upper);
            context.branch(switchMerge);
        }

        // findRangeForLocation already bounded the index, so this arm is unreachable; the
        // OpenCL backend used __builtin_trap here, which has no Vulkan equivalent.
        context.label(defaultLabel);
        context.opVoid(SpirV.OP_UNREACHABLE);

        context.label(switchMerge);

        int nv = context.load(f32, n);
        int ov = context.load(f32, o);
        int onDist = context.op(SpirV.OP_F_SUB, f32, ov, nv);
        int d0 = loadF32(context, derivatives, rangeForLocation);
        int d1 = loadF32(context, derivatives, nextIndex);
        int p = context.op(SpirV.OP_F_SUB, f32, context.op(SpirV.OP_F_MUL, f32, d0, locDist), onDist);
        int q = context.op(SpirV.OP_F_ADD, f32,
                context.op(SpirV.OP_F_MUL, f32, context.op(SpirV.OP_F_NEGATE, f32, d1), locDist), onDist);

        int lerpNo = lerpf(context, k, nv, ov);
        int lerpPq = lerpf(context, k, p, q);
        int oneMinusK = context.op(SpirV.OP_F_SUB, f32, global.constF32(1.0F), k);
        int tail = context.op(SpirV.OP_F_MUL, f32, context.op(SpirV.OP_F_MUL, f32, k, oneMinusK), lerpPq);
        return context.op(SpirV.OP_F_ADD, f32, lerpNo, tail);
    }

    private static int lerpf(SpirVGenFunctionContext context, int delta, int start, int end) {
        return context.op(SpirV.OP_FUNCTION_CALL, context.getGlobalContext().typeFloat(32),
                context.getGlobalContext().preludeFunctionId("math_lerpf"), delta, start, end);
    }

    /**
     * Loads {@code base[index]}. Accesses through a {@code PhysicalStorageBuffer} pointer must
     * carry an explicit alignment.
     */
    private static int loadF32(SpirVGenFunctionContext context, int base, int index) {
        SpirVGenContext global = context.getGlobalContext();
        int f32 = global.typeFloat(32);
        int blockPointer = global.bufferReference(f32, Float.BYTES);
        int elementPointer = global.typePointer(SpirV.STORAGE_CLASS_PHYSICAL_STORAGE_BUFFER, f32);

        int pointer = context.op(SpirV.OP_CONVERT_U_TO_PTR, blockPointer, base);
        int element = context.op(SpirV.OP_ACCESS_CHAIN, elementPointer, pointer, global.constI32(0), index);
        return context.op(SpirV.OP_LOAD, f32, element, SpirV.MEMORY_ACCESS_ALIGNED, Float.BYTES);
    }

    @Override
    public int doSpirVGen(SplineNormalNode node, SpirVGenFunctionContext context) {
        SpirVGenContext global = context.getGlobalContext();
        int f32 = global.typeFloat(32);
        int i32 = global.typeInt(32, true);
        int bool = global.typeBool();


        int lastConst = node.locations.length - 1;
        int locations = SpirVEmitterUtil.constDataAddress(context, global.allocGlobalConstDataObject(node.locations));
        int derivatives = SpirVEmitterUtil.constDataAddress(context, global.allocGlobalConstDataObject(node.derivatives));

        // The location function is evaluated at double precision and narrowed once, as the
        // OpenCL backend does. Evaluating the subtree in float instead loses enough precision to
        // pick a different spline segment, which visibly changes terrain.
        int point = context.op(SpirV.OP_F_CONVERT, f32,
                context.getDelegateVar(context.newVarF64(node.locationFunction)));

        if (node.values.length == 1) {
            return sampleOutsideRange(context, point, locations, value(context, node, 0), derivatives,
                    global.constI32(0));
        }

        int rangeForLocation = context.op(SpirV.OP_FUNCTION_CALL, i32,
                global.preludeFunctionId("df_spline_findRangeForLocation"),
                locations, global.constU32(node.locations.length), point);

        int result = context.newLocal(f32);

        int belowRange = context.op(SpirV.OP_S_LESS_THAN, bool, rangeForLocation, global.constI32(0));
        SpirVEmitterUtil.ifElse(context, belowRange,
                () -> context.store(result, sampleOutsideRange(context, point, locations,
                        value(context, node, 0), derivatives, global.constI32(0))),
                () -> {
                    int atLast = context.op(SpirV.OP_I_EQUAL, bool, rangeForLocation, global.constI32(lastConst));
                    SpirVEmitterUtil.ifElse(context, atLast,
                            () -> context.store(result, sampleOutsideRange(context, point, locations,
                                    value(context, node, lastConst), derivatives, global.constI32(lastConst))),
                            () -> context.store(result, interpolate(context, node, point, locations, derivatives, rangeForLocation)));
                });

        return context.load(f32, result);
    }

}
