package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.base.mixin.access.IStructureWeightSampler;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.SpirVEmitterUtil;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.misc.BeardifierNode;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenContext;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public class BeardifierNodeSpirVEmitter implements SpirVEmitter<BeardifierNode> {

    public static final BeardifierNodeSpirVEmitter INSTANCE = new BeardifierNodeSpirVEmitter();

    private BeardifierNodeSpirVEmitter() {
    }

    @Override
    public int doSpirVGen(BeardifierNode node, SpirVGenFunctionContext context) {
        SpirVGenContext global = context.getGlobalContext();
        int f64 = global.typeFloat(64);
        int u64 = global.typeInt(64, false);
        int bool = global.typeBool();

        int offset = global.getGlobalDynamicDataOffset(DensityFunctionTypes.Beardifier.INSTANCE);
        int tableOffset = global.allocGlobalConstDataObject(IStructureWeightSampler.getSTRUCTURE_WEIGHT_TABLE());

        int result = context.newLocal(f64);
        context.store(result, global.constF64(0.0));

        int rwData = context.paramRwData();
        int hasRwData = context.op(SpirV.OP_I_NOT_EQUAL, bool, rwData, global.constU64(0L));

        SpirVEmitterUtil.ifElse(context, hasRwData,
                () -> {
                    int data = context.op(SpirV.OP_FUNCTION_CALL, u64,
                            global.preludeFunctionId("df_data_offset_global"), rwData, global.constI32(offset));
                    int hasData = context.op(SpirV.OP_I_NOT_EQUAL, bool, data, global.constU64(0L));
                    SpirVEmitterUtil.ifElse(context, hasData,
                            () -> {
                                int table = SpirVEmitterUtil.constDataAddress(context, tableOffset);
                                context.store(result, context.op(SpirV.OP_FUNCTION_CALL, f64,
                                        global.preludeFunctionId("df_structureWeightSampler_sample"),
                                        table, data, context.paramX(), context.paramY(), context.paramZ()));
                            },
                            () -> {
                            });
                },
                () -> {
                });

        return context.load(f64, result);
    }

}
