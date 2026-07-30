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

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineAstNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
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
    public String doCLGen(SplineAstNode node, OpenCLCGenContext context) {
        ValuesMethodDefF valuesMethodDefF = doCLGenSpline(node, context, node.spline, false);
        return "return (double) " + callSpline(valuesMethodDefF, null) + ";\n";
    }

    private static ValuesMethodDefF doCLGenSpline(SplineAstNode node, OpenCLCGenContext context, Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper> spline, boolean cache1) {
        {
            String cachedSplineMethod = context.getCachedSplineMethod(spline, cache1);
            if (cachedSplineMethod != null) {
                return new ValuesMethodDefF(cachedSplineMethod);
            }
        }
        if (spline instanceof Spline.FixedFloatFunction<DensityFunctionTypes.Spline.DensityFunctionWrapper> spline1) {
            return new ValuesMethodDefF(spline1.value());
        }
        String name = context.nextMethodName("Spline");
        StringBuilder predef = new StringBuilder();
        StringBuilder body = new StringBuilder();
        body.append(context.getFillerOrNot());

        boolean noinline = false;

        if (spline instanceof Spline.Implementation<DensityFunctionTypes.Spline.DensityFunctionWrapper> impl) {

            String locations = name + "_locations";
            String derivatives = name + "_derivatives";
            predef.append("constant const float ").append(locations).append("[").append(impl.locations().length).append("] = ").append(literal(impl.locations())).append(";\n");
            predef.append("constant const float ").append(derivatives).append("[").append(impl.derivatives().length).append("] = ").append(literal(impl.derivatives())).append(";\n");

            int lastConst = impl.locations().length - 1;

            if (cache1) {
                body.append("float point = cache1;\n");
            } else {
                ValuesMethodDefD locationFunction = context.newMethod(node.children.get(impl.locationFunction()));
                body.append("float point = (float) ").append(context.callDelegate(locationFunction)).append(";\n");
            }

            int valuesMethodsLength = impl.values().size();
            if (valuesMethodsLength == 1) {
                body
                        .append("return df_spline_sampleOutsideRange_const(point, ")
                        .append(locations).append(", ")
                        .append(callSpline(doCLGenSpline(node, context, impl.values().getFirst(), false), null)).append(", ")
                        .append(derivatives).append(", 0);\n");
            } else {
                body
                        .append("float cache1Local;\n");

                AstNode allSameLocationFunction = SplineAstNode.needOptimizeSameLocationFunction(node, impl.values().toArray(Spline[]::new));
                if (allSameLocationFunction != null) {
                    body.append("cache1Local = (float) ").append(context.callDelegate(context.newMethod(allSameLocationFunction))).append(";\n");
                } else {
                    if (impl.values().stream().filter(child -> child instanceof Spline.Implementation<DensityFunctionTypes.Spline.DensityFunctionWrapper>).count() > 5) { // some abitrary number
                        noinline = true;
                    }
                }

                body
                        .append("int32_t rangeForLocation = df_spline_findRangeForLocation_const(").append(locations).append(", ").append(impl.locations().length).append(", ").append("point);\n")
                        .append("if (rangeForLocation < 0) {\n")
                        .append("    ").append("return df_spline_sampleOutsideRange_const(point, ").append(locations).append(", ").append(callSpline(doCLGenSpline(node, context, impl.values().getFirst(), allSameLocationFunction != null), allSameLocationFunction != null ? "cache1Local" : null)).append(", ").append(derivatives).append(", 0);\n")
                        .append("} else if (rangeForLocation == ").append(lastConst).append(") {\n")
                        .append("    ").append("return df_spline_sampleOutsideRange_const(point, ").append(locations).append(", ").append(callSpline(doCLGenSpline(node, context, impl.values().get(lastConst), allSameLocationFunction != null), allSameLocationFunction != null ? "cache1Local" : null)).append(", ").append(derivatives).append(", ").append(lastConst).append(");\n")
                        .append("}\n")
                        .append("float loc0 = ").append(locations).append("[rangeForLocation];\n")
                        .append("float loc1 = ").append(locations).append("[rangeForLocation + 1];\n")
                        .append("float locDist = loc1 - loc0;\n")
                        .append("float k = (point - loc0) / locDist;\n")
                        .append("float n, o;\n");

                body
                        .append("switch (rangeForLocation) {\n");

                boolean[] jumpGenerated = new boolean[valuesMethodsLength - 1];

                for (int i = 0; i < valuesMethodsLength - 1; i++) {
                    if (jumpGenerated[i]) continue;
                    body.append("    ").append("case ").append(i).append(":\n");
                    jumpGenerated[i] = true;
                    for (int j = i + 1; j < valuesMethodsLength - 1; j++) { // deduplication
                        if (impl.values().get(i).equals(impl.values().get(j)) && impl.values().get(i + 1).equals(impl.values().get(j + 1))) {
                            body.append("    ").append("case ").append(j).append(":\n");
                            jumpGenerated[j] = true;
                        }
                    }

                    boolean optimizePure = impl.values().get(i).equals(impl.values().get(i + 1));
                    AstNode sameLocationFunction;
                    boolean doCache1;
                    if (allSameLocationFunction != null) {
                        sameLocationFunction = null; // avoid double calc
                        doCache1 = true;
                    } else {
                        sameLocationFunction = SplineAstNode.needOptimizeSameLocationFunction(node, impl.values().get(i), impl.values().get(i + 1));
                        doCache1 = sameLocationFunction != null;
                    }
                    if (optimizePure) { // splines are pure
                        if (doCache1) { // to encourage compiler to do loop common extraction maybe
                            if (sameLocationFunction != null) { // avoid double calc
                                body.append("    ").append("    ").append("cache1Local = (float) ").append(context.callDelegate(context.newMethod(sameLocationFunction))).append(";\n");
                            }
                            body.append("    ").append("    ").append("n = o = ").append(callSpline(doCLGenSpline(node, context, impl.values().get(i), true), "cache1Local")).append(";\n");
                        } else {
                            body.append("    ").append("    ").append("n = o = ").append(callSpline(doCLGenSpline(node, context, impl.values().get(i), false), null)).append(";\n");
                        }
                    } else {
                        if (doCache1) {
                            if (sameLocationFunction != null) { // avoid double calc
                                body.append("    ").append("    ").append("cache1Local = (float) ").append(context.callDelegate(context.newMethod(sameLocationFunction))).append(";\n");
                            }
                            body.append("    ").append("    ").append("n = ").append(callSpline(doCLGenSpline(node, context, impl.values().get(i), true), "cache1Local")).append(";\n");
                            body.append("    ").append("    ").append("o = ").append(callSpline(doCLGenSpline(node, context, impl.values().get(i + 1), true), "cache1Local")).append(";\n");
                        } else {
                            body.append("    ").append("    ").append("n = ").append(callSpline(doCLGenSpline(node, context, impl.values().get(i), false), null)).append(";\n");
                            body.append("    ").append("    ").append("o = ").append(callSpline(doCLGenSpline(node, context, impl.values().get(i + 1), false), null)).append(";\n");
                        }
                    }
                    body.append("    ").append("    ").append("break;\n");
                }

                body
                        .append("    ").append("default:\n")
                        .append("    ").append("    ").append("__builtin_trap();\n")
                        .append("    ").append("    ").append("return nan((uint64_t) 0);\n") // unreachable
                        .append("}\n")
                        .append("float onDist = o - n;\n")
                        .append("float p = ").append(derivatives).append("[rangeForLocation] * locDist - onDist;\n")
                        .append("float q = -").append(derivatives).append("[rangeForLocation + 1] * locDist + onDist;\n")
                        .append("return math_lerpf(k, n, o) + k * (1.0F - k) * math_lerpf(k, p, q);\n");
            }
        } else if (spline instanceof Spline.FixedFloatFunction<DensityFunctionTypes.Spline.DensityFunctionWrapper> floatFunction) {
            body.append("return ").append(literal(floatFunction.value())).append(";\n");
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported spline implementation: %s", spline.getClass().getName()));
        }

        context.appendRaw(
                new StringBuilder()
                        .append(predef)
                        .append("static __attribute__((pure))").append(noinline ? " FUNC_NOINLINE_MIDDF" : "").append(" float ").append(name).append(cache1 ? SPLINE_OCL_METHOD_DESC_CACHE1 : SPLINE_OCL_METHOD_DESC).append(" {\n")
                        .append(body.toString().indent(4))
                        .append("}\n")
                        .toString()
        );

        context.cacheSplineMethod(spline, name, cache1);

        return new ValuesMethodDefF(name);
    }

    private static String callSpline(ValuesMethodDefF target, String cache1Arg) {
        if (target.isConst()) {
            return literal(target.constValue());
        } else if (cache1Arg != null) {
            return target.generatedMethod() + "(ctx, " + cache1Arg + ")";
        } else {
            return target.generatedMethod() + "(ctx)";
        }
    }
}
