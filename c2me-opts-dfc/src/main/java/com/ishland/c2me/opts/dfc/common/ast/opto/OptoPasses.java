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

package com.ishland.c2me.opts.dfc.common.ast.opto;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import com.ishland.c2me.opts.dfc.common.ast.opto.passes.BranchElimination;
import com.ishland.c2me.opts.dfc.common.ast.opto.passes.CacheElimination;
import com.ishland.c2me.opts.dfc.common.ast.opto.passes.FoldConstants;
import com.ishland.c2me.opts.dfc.common.ast.opto.passes.TreeNormalization;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class OptoPasses {

    private static final AstTransformer[] PASSES = new AstTransformer[] {
            TreeNormalization.INSTANCE,
            FoldConstants.INSTANCE,
            BranchElimination.INSTANCE,
    };

    private static final AstTransformer[] PASSES_OCL = new AstTransformer[] {
            CacheElimination.INSTANCE,
            TreeNormalization.INSTANCE,
            FoldConstants.INSTANCE,
            BranchElimination.INSTANCE,
    };

    public static AstPair optimize(AstNode astNode) {
        return optimize0(astNode, PASSES);
    }

    public static AstPair optimizeOCL(AstNode astNode) {
        return optimize0(astNode, PASSES_OCL);
    }

    private static @NonNull AstPair optimize0(AstNode astNode, AstTransformer[] passes) {
        AstNode unoptimized = astNode;
        AstNode res = astNode;
        do {
            astNode = res;
            for (AstTransformer pass : passes) {
                res = res.transform(pass);
            }
        } while (res != astNode);
        return new AstPair(unoptimized, res);
    }

    public record AstPair(@Nullable AstNode unoptimized, @NonNull AstNode optimized) {

        public static AstPair ofOptimizedOnly(AstNode optimized) {
            return new AstPair(null, optimized);
        }

        public AstNode tryUnoptimized() {
            return this.unoptimized != null ? this.unoptimized : this.optimized;
        }

    }

}
