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

package com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.integration.lithostitched.misc;

import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.MixNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import org.jetbrains.annotations.UnknownNullability;

import static com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen.literal;

public class MixNodeOpenCLCEmitter implements OpenCLCEmitter<MixNode> {
    public static final MixNodeOpenCLCEmitter INSTANCE = new MixNodeOpenCLCEmitter();

    private MixNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(MixNode node, @UnknownNullability OpenCLCGenContext context) {
        ValuesMethodDefD input = context.newMethod(node.input);
        ValuesMethodDefD argument1 = context.newMethod(node.argument1);
        ValuesMethodDefD argument2 = context.newMethod(node.argument2);
        return "double v = " + context.callDelegate(input) + ";\n" +
                "double a = " + context.callDelegate(argument1) + ";\n" +
                "double b = " + context.callDelegate(argument2) + ";\n" +
                "return a * (1 - v) + b * v;\n";
    }
}
