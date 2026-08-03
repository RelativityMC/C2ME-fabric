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
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineNormalNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenFunctionContext;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

import static com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen.literal;


public class SplineNormalNodeOpenCLCEmitter implements OpenCLCEmitter<SplineNormalNode> {
    public static final SplineNormalNodeOpenCLCEmitter INSTANCE = new SplineNormalNodeOpenCLCEmitter();

    private SplineNormalNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(SplineNormalNode node, OpenCLCGenFunctionContext context, String storeTo) {
        StringBuilder body = new StringBuilder();
        String locations = storeTo + "_locations";
        String derivatives = storeTo + "_derivatives";
        body.append("const float ").append(locations).append("[").append(node.locations.length).append("] = ").append(literal(node.locations)).append(";\n");
        body.append("const float ").append(derivatives).append("[").append(node.derivatives.length).append("] = ").append(literal(node.derivatives)).append(";\n");

        int lastConst = node.locations.length - 1;

        {
            OpenCLCGenFunctionContext forked = context.fork();
            ValuesMethodDefF64 locationFunction = forked.newVarF64(node.locationFunction);
            body.append(forked.getBody());
            body.append("float point = (float) ").append(forked.getDelegateVar(locationFunction)).append(";\n");
        }

        int valuesMethodsLength = node.values.length;
        if (valuesMethodsLength == 1) {
            body
                    .append(storeTo).append(" = df_spline_sampleOutsideRange(point, ")
                    .append(locations).append(", ")
                    .append(context.getDelegateVar(context.newVarF32(node.values[0]))).append(", ")
                    .append(derivatives).append(", 0);\n");
        } else {
            body
                    .append("int32_t rangeForLocation = df_spline_findRangeForLocation(").append(locations).append(", ").append(node.locations.length).append(", ").append("point);\n")
                    .append("if (rangeForLocation < 0) {\n")
                    .append("    ").append(storeTo).append(" = df_spline_sampleOutsideRange(point, ").append(locations).append(", ").append(context.getDelegateVar(context.newVarF32(node.values[0]))).append(", ").append(derivatives).append(", 0);\n")
                    .append("} else if (rangeForLocation == ").append(lastConst).append(") {\n")
                    .append("    ").append(storeTo).append(" = df_spline_sampleOutsideRange(point, ").append(locations).append(", ").append(context.getDelegateVar(context.newVarF32(node.values[lastConst]))).append(", ").append(derivatives).append(", ").append(lastConst).append(");\n")
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
                    if (node.values[i].equals(node.values[j]) && node.values[i + 1].equals(node.values[j + 1])) {
                        body.append("    ").append("    ").append("case ").append(j).append(":\n");
                        jumpGenerated[j] = true;
                    }
                }

                body.append("    ").append("    ").append("    ").append("n = ").append(context.getDelegateVar(context.newVarF32(node.values[i]))).append(";\n");
                body.append("    ").append("    ").append("    ").append("o = ").append(context.getDelegateVar(context.newVarF32(node.values[i + 1]))).append(";\n");
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

        return body.toString();
    }
}
