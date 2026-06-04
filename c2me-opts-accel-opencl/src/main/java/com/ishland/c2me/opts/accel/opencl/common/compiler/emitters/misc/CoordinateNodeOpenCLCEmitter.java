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

import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateNode;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;

public class CoordinateNodeOpenCLCEmitter implements OpenCLCEmitter<CoordinateNode> {
    public static final CoordinateNodeOpenCLCEmitter INSTANCE = new CoordinateNodeOpenCLCEmitter();

    private CoordinateNodeOpenCLCEmitter() {
    }

    @Override
    public String doCLGen(CoordinateNode node, OpenCLCGenContext context) {
        return switch (node.axis) {
            case X -> "return (double) ctx.x;\n";
            case Y -> "return (double) ctx.y;\n";
            case Z -> "return (double) ctx.z;\n";
        };
    }
}
