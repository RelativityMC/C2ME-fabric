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

import com.ishland.c2me.base.mixin.access.IStructureWeightSampler;
import com.ishland.c2me.opts.dfc.common.ast.misc.BeardifierNode;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.jetbrains.annotations.UnknownNullability;

public class BeardifierNodeOpenCLCEmitter implements OpenCLCEmitter<BeardifierNode> {
    public static final BeardifierNodeOpenCLCEmitter INSTANCE = new BeardifierNodeOpenCLCEmitter();

    private BeardifierNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(BeardifierNode node, @UnknownNullability OpenCLCGenContext context) {
        int offset = context.getGlobalDynamicDataOffset(DensityFunctionTypes.Beardifier.INSTANCE);
        int tableOffset = context.allocGlobalConstDataObject(IStructureWeightSampler.getSTRUCTURE_WEIGHT_TABLE());
        return "if (!ctx.rw_data) return 0.0;\n" +
                "global const sws_index_t * restrict data = df_data_offset_global(ctx.rw_data, " + offset + ");\n" +
                "global const float * restrict structureWeightSamplerTable = ptr_shift_global(ctx.const_data, " + tableOffset + ");\n" +
                "if (!data) return 0.0;\n" +
                "return df_structureWeightSampler_sample(structureWeightSamplerTable, data, ctx.x, ctx.y, ctx.z);\n";
    }
}
