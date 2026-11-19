/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
