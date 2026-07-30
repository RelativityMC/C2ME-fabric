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

import com.ishland.c2me.opts.dfc.common.ast.misc.YClampedGradientNode;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;

import static com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen.literal;

public class YClampedGradientNodeOpenCLCEmitter implements OpenCLCEmitter<YClampedGradientNode> {
    public static final YClampedGradientNodeOpenCLCEmitter INSTANCE = new YClampedGradientNodeOpenCLCEmitter();

    private YClampedGradientNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(YClampedGradientNode node, OpenCLCGenContext context) {
        return "return math_clampedMap((double) ctx.y, " + literal(node.fromY) + ", " + literal(node.toY) + ", " + literal(node.fromValue) + ", " + literal(node.toValue) + ");\n";
    }
}
