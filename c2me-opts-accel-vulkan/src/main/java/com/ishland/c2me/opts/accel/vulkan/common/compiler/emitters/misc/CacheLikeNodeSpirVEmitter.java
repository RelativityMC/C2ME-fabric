package com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc;

import com.ishland.c2me.opts.accel.vulkan.common.compiler.SpirVGen;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.SpirVEmitterUtil;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeNode;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenContext;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public class CacheLikeNodeSpirVEmitter implements SpirVEmitter<CacheLikeNode> {

    public static final CacheLikeNodeSpirVEmitter INSTANCE = new CacheLikeNodeSpirVEmitter();

    private static final int MASK_ENABLE_FLAT_CACHE = 1;
    private static final int MASK_ENABLE_ALL_CACHES = 3;

    private static final int MEMBER_CACHED = 0;
    private static final int MEMBER_RES = 1;

    private CacheLikeNodeSpirVEmitter() {
    }

    private static int delegate(SpirVGenFunctionContext context, CacheLikeNode node) {
        return context.getDelegateVar(context.newVarF64(node.getDelegate()));
    }

    private static int emitCache(CacheLikeNode node, SpirVGenFunctionContext context, String helper,
                                 int mask, int offset, int ordinal) {
        SpirVGenContext global = context.getGlobalContext();
        int f64 = global.typeFloat(64);
        int u32 = global.typeInt(32, false);
        int bool = global.typeBool();

        int result = context.newLocal(f64);

        int rwData = context.paramRwData();
        int hasRwData = context.op(SpirV.OP_I_NOT_EQUAL, bool, rwData, global.constU64(0L));
        int maskConst = global.constU32(mask);
        int masked = context.op(SpirV.OP_BITWISE_AND, u32, context.paramSampleFlags(), maskConst);
        int enabled = context.op(SpirV.OP_I_EQUAL, bool, masked, maskConst);
        int usable = context.op(SpirV.OP_LOGICAL_AND, bool, hasRwData, enabled);

        SpirVEmitterUtil.ifElse(context, usable,
                () -> {
                    int data = context.op(SpirV.OP_FUNCTION_CALL, global.typeInt(64, false),
                            global.preludeFunctionId("df_data_offset_global"), rwData, global.constI32(offset));
                    int cacheResult = context.op(SpirV.OP_FUNCTION_CALL, global.typeSampleCacheResult(),
                            global.preludeFunctionId(helper),
                            rwData, data, global.constU32(ordinal),
                            context.paramX(), context.paramY(), context.paramZ(), context.paramSampleFlags());
                    int cached = context.op(SpirV.OP_COMPOSITE_EXTRACT, bool, cacheResult, MEMBER_CACHED);
                    int value = context.op(SpirV.OP_COMPOSITE_EXTRACT, f64, cacheResult, MEMBER_RES);
                    context.store(result, context.op(SpirV.OP_SELECT, f64, cached, value,
                            global.constF64(Double.NaN)));
                },
                () -> context.store(result, global.constF64(Double.NaN)));

        return context.load(f64, result);
    }

    @Override
    public int doSpirVGen(CacheLikeNode node, SpirVGenFunctionContext context) {
        if (!((Object) node.getCacheLike() instanceof DensityFunctionTypes.Wrapping wrapping)) {
            throw new UnsupportedOperationException("Can only gen wrapping");
        }
        SpirVGenContext global = context.getGlobalContext();
        return switch (wrapping.type()) {
            case CACHE_ONCE, CACHE_ALL_IN_CELL -> delegate(context, node);
            case INTERPOLATED -> context.getVariant().enableAllCache
                    ? emitCache(node, context, "df_cachelike_interpolator", MASK_ENABLE_ALL_CACHES,
                    global.getGlobalDynamicDataOffset(SpirVGen.MARKER_cacheLike_interpolator),
                    global.registerInterpolator(node))
                    : delegate(context, node);
            case FLAT_CACHE -> context.getVariant().enableFlatCache
                    ? emitCache(node, context, "df_cachelike_flatcache", MASK_ENABLE_FLAT_CACHE,
                    global.getGlobalDynamicDataOffset(SpirVGen.MARKER_cacheLike_flatCache),
                    global.registerFlatCache(node))
                    : delegate(context, node);
            case CACHE2D -> context.getVariant().useCache2D()
                    ? emitCache(node, context, "df_cachelike_cache2d", MASK_ENABLE_ALL_CACHES,
                    global.getGlobalDynamicDataOffset(SpirVGen.MARKER_cacheLike_cache2d),
                    global.registerCache2d(node))
                    : delegate(context, node);
            case BLEND_DENSITY -> throw new UnsupportedOperationException("BLEND_DENSITY should not be here");
        };
    }

}
