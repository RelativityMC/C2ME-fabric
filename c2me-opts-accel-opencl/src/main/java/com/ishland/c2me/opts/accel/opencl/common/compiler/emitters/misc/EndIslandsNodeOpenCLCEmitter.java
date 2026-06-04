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

import com.ishland.c2me.base.mixin.access.IDensityFunctionTypesEndIslands;
import com.ishland.c2me.base.mixin.access.ISimplexNoiseSampler;
import com.ishland.c2me.opts.dfc.common.ast.misc.EndIslandsNode;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import org.jetbrains.annotations.UnknownNullability;

public class EndIslandsNodeOpenCLCEmitter implements OpenCLCEmitter<EndIslandsNode> {
    public static final EndIslandsNodeOpenCLCEmitter INSTANCE = new EndIslandsNodeOpenCLCEmitter();

    private EndIslandsNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(EndIslandsNode node, @UnknownNullability OpenCLCGenContext context) {
        int[] permutation = ((ISimplexNoiseSampler) ((IDensityFunctionTypesEndIslands) (Object) node.endIslands).getSampler()).getPermutation();
        int offset = context.allocGlobalConstDataObject(permutation);
        return "global const uint32_t * const permutation = ptr_shift_global(ctx.const_data, " + offset + ");\n" +
                "return ((double) math_end_islands_sample_global(permutation, ctx.x / 8, ctx.z / 8) - 8.0) / 128.0;\n";
    }
}
