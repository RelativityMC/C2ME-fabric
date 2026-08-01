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

import com.ishland.c2me.opts.dfc.common.ast.spline.SplineAstNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenFunctionContext;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

import static com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen.literal;


public class SplineAstNodeOpenCLCEmitter implements OpenCLCEmitter<SplineAstNode> {
    public static final SplineAstNodeOpenCLCEmitter INSTANCE = new SplineAstNodeOpenCLCEmitter();

    private SplineAstNodeOpenCLCEmitter() {
    }

    public static final String SPLINE_OCL_METHOD_DESC = "(const sample_int32_ctx_t ctx)";
    public static final String SPLINE_OCL_METHOD_DESC_CACHE1 = "(const sample_int32_ctx_t ctx, const float cache1)";

    @Override
    public String doCLGen(SplineAstNode node, OpenCLCGenFunctionContext context, String storeTo) {
        ValuesMethodDefF32 valuesMethodDefF32 = doCLGenSpline(node, context, node.spline);
        return storeTo + " = (double) " + getDelegateSplineVar(valuesMethodDefF32) + ";\n";
    }

    private static ValuesMethodDefF32 doCLGenSpline(SplineAstNode node, OpenCLCGenFunctionContext context, Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper> spline) {
        {
            String cachedSplineMethod = context.getCachedSplineVar(spline);
            if (cachedSplineMethod != null) {
                return new ValuesMethodDefF32(cachedSplineMethod);
            }
        }
        if (spline instanceof Spline.FixedFloatFunction<DensityFunctionTypes.Spline.DensityFunctionWrapper> spline1) {
            return new ValuesMethodDefF32(spline1.value());
        }
        String storeTo = context.nextVarName();
        StringBuilder body = new StringBuilder();

        boolean noinline = false;

        if (spline instanceof Spline.Implementation<DensityFunctionTypes.Spline.DensityFunctionWrapper> impl) {

            String locations = storeTo + "_locations";
            String derivatives = storeTo + "_derivatives";
            body.append("const float ").append(locations).append("[").append(impl.locations().length).append("] = ").append(literal(impl.locations())).append(";\n");
            body.append("const float ").append(derivatives).append("[").append(impl.derivatives().length).append("] = ").append(literal(impl.derivatives())).append(";\n");

            int lastConst = impl.locations().length - 1;

            {
                OpenCLCGenFunctionContext forked = context.fork();
                ValuesMethodDefF64 locationFunction = forked.newVar(node.children.get(impl.locationFunction()));
                body.append(forked.getBody());
                body.append("float point = (float) ").append(forked.getDelegateVar(locationFunction)).append(";\n");
            }

            int valuesMethodsLength = impl.values().size();
            if (valuesMethodsLength == 1) {
                body
                        .append(storeTo).append(" = df_spline_sampleOutsideRange(point, ")
                        .append(locations).append(", ")
                        .append(getDelegateSplineVar(doCLGenSpline(node, context, impl.values().getFirst()))).append(", ")
                        .append(derivatives).append(", 0);\n");
            } else {
                body
                        .append("int32_t rangeForLocation = df_spline_findRangeForLocation(").append(locations).append(", ").append(impl.locations().length).append(", ").append("point);\n")
                        .append("if (rangeForLocation < 0) {\n")
                        .append("    ").append(storeTo).append(" = df_spline_sampleOutsideRange(point, ").append(locations).append(", ").append(getDelegateSplineVar(doCLGenSpline(node, context, impl.values().getFirst()))).append(", ").append(derivatives).append(", 0);\n")
                        .append("} else if (rangeForLocation == ").append(lastConst).append(") {\n")
                        .append("    ").append(storeTo).append(" = df_spline_sampleOutsideRange(point, ").append(locations).append(", ").append(getDelegateSplineVar(doCLGenSpline(node, context, impl.values().get(lastConst)))).append(", ").append(derivatives).append(", ").append(lastConst).append(");\n")
                        .append("} else {\n")
                        .append("    ").append("float loc0 = ").append(locations).append("[rangeForLocation];\n")
                        .append("    ").append("float loc1 = ").append(locations).append("[rangeForLocation + 1];\n")
                        .append("    ").append("float locDist = loc1 - loc0;\n")
                        .append("    ").append("float k = (point - loc0) / locDist;\n")
                        .append("    ").append("float n, o;\n");

                body
                        .append("    ").append("switch (rangeForLocation) {\n");

                boolean[] jumpGenerated = new boolean[valuesMethodsLength - 1];

                for (int i = 0; i < valuesMethodsLength - 1; i++) {
                    if (jumpGenerated[i]) continue;
                    body.append("    ").append("    ").append("case ").append(i).append(":\n");
                    jumpGenerated[i] = true;
                    for (int j = i + 1; j < valuesMethodsLength - 1; j++) { // deduplication
                        if (impl.values().get(i).equals(impl.values().get(j)) && impl.values().get(i + 1).equals(impl.values().get(j + 1))) {
                            body.append("    ").append("    ").append("case ").append(j).append(":\n");
                            jumpGenerated[j] = true;
                        }
                    }

                    body.append("    ").append("    ").append("    ").append("n = ").append(getDelegateSplineVar(doCLGenSpline(node, context, impl.values().get(i)))).append(";\n");
                    body.append("    ").append("    ").append("    ").append("o = ").append(getDelegateSplineVar(doCLGenSpline(node, context, impl.values().get(i + 1)))).append(";\n");
                    body.append("    ").append("    ").append("    ").append("break;\n");
                }

                body
                        .append("    ").append("    ").append("default:\n")
                        .append("    ").append("    ").append("    ").append("__builtin_trap();\n")
                        .append("    ").append("    ").append("    ").append("__builtin_unreachable();\n")
                        .append("    ").append("    ").append("    ").append("n = o = nan((uint64_t) 0);\n") // unreachable
                        .append("    ").append("}\n")
                        .append("    ").append("float onDist = o - n;\n")
                        .append("    ").append("float p = ").append(derivatives).append("[rangeForLocation] * locDist - onDist;\n")
                        .append("    ").append("float q = -").append(derivatives).append("[rangeForLocation + 1] * locDist + onDist;\n")
                        .append("    ").append(storeTo).append(" = math_lerpf(k, n, o) + k * (1.0F - k) * math_lerpf(k, p, q);\n")
                        .append("}\n");
            }
        } else if (spline instanceof Spline.FixedFloatFunction<DensityFunctionTypes.Spline.DensityFunctionWrapper> floatFunction) {
            body.append(storeTo).append(" = ").append(literal(floatFunction.value())).append(";\n");
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported spline implementation: %s", spline.getClass().getName()));
        }

        context.appendRaw(
                new StringBuilder()
                        .append("float ").append(storeTo).append(";\n")
                        .append("{\n")
                        .append(body.toString().indent(4))
                        .append("}\n")
                        .toString()
        );

        context.cacheSplineVar(spline, storeTo);

        return new ValuesMethodDefF32(storeTo);
    }

    private static String getDelegateSplineVar(ValuesMethodDefF32 target) {
        if (target.isConst()) {
            return literal(target.constValue());
        } else {
            return target.generatedMethod();
        }
    }
}
