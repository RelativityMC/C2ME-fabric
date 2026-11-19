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

package com.ishland.c2me.opts.dfc.common.ast.opto.passes;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceNode;

public class BranchElimination implements AstTransformer {

    public static final BranchElimination INSTANCE = new BranchElimination();

    private BranchElimination() {
    }

    @Override
    public AstNode transform(AstNode astNode) {
        return switch (astNode) {
            case RangeChoiceNode rangeChoiceNode -> {
                if (rangeChoiceNode.input instanceof ConstantNode c) {
                    if (c.getValue() >= rangeChoiceNode.minInclusive && c.getValue() < rangeChoiceNode.maxExclusive) {
                        yield rangeChoiceNode.whenInRange;
                    } else {
                        yield rangeChoiceNode.whenOutOfRange;
                    }
                }

                if (rangeChoiceNode.whenInRange.equals(rangeChoiceNode.whenOutOfRange)) {
                    yield rangeChoiceNode.whenInRange;
                }

                yield rangeChoiceNode;
            }
            default -> astNode;
        };
    }

}
