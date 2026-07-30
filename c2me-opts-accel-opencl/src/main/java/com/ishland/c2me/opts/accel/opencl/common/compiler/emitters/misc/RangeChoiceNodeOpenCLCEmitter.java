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

import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import org.jetbrains.annotations.UnknownNullability;

import static com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen.literal;

public class RangeChoiceNodeOpenCLCEmitter implements OpenCLCEmitter<RangeChoiceNode> {
    public static final RangeChoiceNodeOpenCLCEmitter INSTANCE = new RangeChoiceNodeOpenCLCEmitter();

    private RangeChoiceNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(RangeChoiceNode node, @UnknownNullability OpenCLCGenContext context) {
        ValuesMethodDefD input = context.newMethod(node.input);
        ValuesMethodDefD whenInRange = context.newMethod(node.whenInRange);
        ValuesMethodDefD whenOutOfRange = context.newMethod(node.whenOutOfRange);
        return "double v = " + context.callDelegate(input) + ";\n" +
                "return (v >= " + literal(node.minInclusive) + " && v < " + literal(node.maxExclusive) + ") ? " + context.callDelegate(whenInRange) + " : " + context.callDelegate(whenOutOfRange) + ";\n";
    }
}
