/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.dfc.common.gen.dot.emitters.misc;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantF32Node;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineNormalNode;
import com.ishland.c2me.opts.dfc.common.gen.dot.DotEmitter;
import com.ishland.c2me.opts.dfc.common.gen.dot.DotGen;

public class SplineNormalNodeDotEmitter implements DotEmitter<SplineNormalNode> {
    public static final SplineNormalNodeDotEmitter INSTANCE = new SplineNormalNodeDotEmitter();

    private SplineNormalNodeDotEmitter() {
    }

    @Override
    public int doDotGen(SplineNormalNode node, DotGen.Context context, DotGen.Context.Builder builder) {
        builder
                .hexagonShape()
                .label("SplineNormal")
//                    .label(String.format("Spline\\nderivatives=%s\\nlocations=%s", Arrays.toString(a1.derivatives()), Arrays.toString(a1.locations())))
                .edge(context.generate(node.locationFunction)).label("locationFunction").finish();

        DotGen.Context.Builder tableBuilder = context.createExtraBuilder();

        StringBuilder table = new StringBuilder();
        table.append('<');
        table.append("<TABLE>");
        table.append("<TR><TD>idx</TD><TD>derivatives</TD><TD>locations</TD><TD>values</TD></TR>");

        AstNode[] values = node.values;
        for (int i = 0, valuesSize = values.length; i < valuesSize; i++) {
            AstNode child = values[i];
            table.append("<TR>")
                    .append("<TD>").append(i).append("</TD>")
                    .append("<TD>").append(node.derivatives[i]).append("</TD>")
                    .append("<TD>").append(node.locations[i]).append("</TD>");

            if (child instanceof ConstantF32Node constantF32Node) {
                table.append("<TD>").append(constantF32Node.getValue()).append("</TD>");
            } else {
                int childId = context.generate(child);
                tableBuilder.edge(childId).label(String.format("children[%d]", i)).finish();
                table.append("<TD>").append("children.id=").append(DotGen.Context.base26(childId)).append("</TD>");
            }

            table.append("</TR>");
        }

        table.append("</TABLE>");
        table.append(">");

        int tableId = tableBuilder
                .boxShape()
                .label(table.toString())
                .build();

        builder.edge(tableId).label("SplineTable").finish();

        return builder.build();
    }
}
