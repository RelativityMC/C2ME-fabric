/*
 * All Rights Reserved
 *
 * Copyright (c) 2025-2026 ishland
 *
 * All rights reserved. Do not redistribute.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc;

import com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

public class CacheLikeNodeOpenCLCEmitter implements OpenCLCEmitter<CacheLikeNode> {
    public static final CacheLikeNodeOpenCLCEmitter INSTANCE = new CacheLikeNodeOpenCLCEmitter();

    private CacheLikeNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(CacheLikeNode node, OpenCLCGenContext context) {
        if (!((Object) node.getCacheLike() instanceof DensityFunctionTypes.Wrapping wrapping)) {
            throw new UnsupportedOperationException("Can only gen wrapping");
        }
        ValuesMethodDefD valuesMethodDefD = context.newMethod(node.getDelegate());
        return switch (wrapping.type()) {
            case CACHE_ONCE, CACHE_ALL_IN_CELL -> "return " + context.callDelegate(valuesMethodDefD) + ";";
            case INTERPOLATED -> {
                int offset = context.getGlobalDynamicDataOffset(OpenCLCGen.MARKER_cacheLike_interpolator);
                int ordinal = context.registerInterpolator(node);
                yield "if (ctx.rw_data && (ctx.sample_flags & MASK_inInterpolationLoop) && (ctx.sample_flags & MASK_isInterpolation)) {\n" +
                        "    global const worldgen_params_t * restrict params = ctx.rw_data;\n" +
                        "    global double * restrict interpolator_buffer = df_data_offset_global(ctx.rw_data, " + offset + ");\n" +
                        "    const cache_result_t res = df_cachelike_interpolator(params, interpolator_buffer, " + ordinal + ", ctx.x, ctx.y, ctx.z, ctx.sample_flags);\n" +
                        "    if (res.cached) {\n" +
                        "        return res.res;\n" +
                        "    }\n" +
                        "    df_cachelike_trap_printf(\"interpolator\", ctx);\n" +
                        "    __builtin_trap();\n" +
                        "    __builtin_unreachable();\n" +
                        "    return nan((uint64_t) 0);\n" +
                        "}\n" +
                        "return " + context.callDelegate(valuesMethodDefD) + ";\n";
            }
            case FLAT_CACHE -> {
                int offset = context.getGlobalDynamicDataOffset(OpenCLCGen.MARKER_cacheLike_flatCache);
                int ordinal = context.registerFlatCache(node);
                yield "if (ctx.rw_data) {\n" +
                        "    global const worldgen_params_t * restrict params = ctx.rw_data;\n" +
                        "    global const double * restrict data = df_data_offset_global(ctx.rw_data, " + offset + ");\n" +
                        "    const cache_result_t res = df_cachelike_flatcache(params, data, " + ordinal + ", ctx.x, ctx.y, ctx.z);\n" +
                        "    if (res.cached) {\n" +
                        "        return res.res;\n" +
                        "    }\n" +
                        "    df_cachelike_trap_printf(\"flatcache\", ctx);\n" +
                        "    __builtin_trap();\n" +
                        "    __builtin_unreachable();\n" +
                        "    return nan((uint64_t) 0);\n" +
                        "}\n" +
                        "return " + context.callDelegate(valuesMethodDefD) + ";\n";
            }
            case CACHE2D -> {
                int offset = context.getGlobalDynamicDataOffset(OpenCLCGen.MARKER_cacheLike_cache2d);
                int ordinal = context.registerCache2d(node);
                yield "if (ctx.rw_data && (ctx.sample_flags & MASK_inInterpolationLoop) && (ctx.sample_flags & MASK_isInterpolation) && (ctx.sample_flags & MASK_interpolationEnableCache2D)) {\n" +
                        "    global const worldgen_params_t * restrict params = ctx.rw_data;\n" +
                        "    global const double * restrict data = df_data_offset_global(ctx.rw_data, " + offset + ");\n" +
                        "    const cache_result_t res = df_cachelike_cache2d(params, data, " + ordinal + ", ctx.x, ctx.y, ctx.z, ctx.sample_flags);\n" +
                        "    if (res.cached) {\n" +
                        "        return res.res;\n" +
                        "    }\n" +
                        "    df_cachelike_trap_printf(\"cache2d\", ctx);\n" +
                        "    __builtin_trap();\n" +
                        "    __builtin_unreachable();\n" +
                        "    return nan((uint64_t) 0);\n" +
                        "}\n" +
                        "return " + context.callDelegate(valuesMethodDefD) + ";\n";
            }
            case BLEND_DENSITY -> throw new UnsupportedOperationException("BLEND_DENSITY should not be here");
        };
    }
}
