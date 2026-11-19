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

package com.ishland.c2me.opts.dfc.common.gen.opencl;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;

public interface OpenCLCGenFunctionContext {
    OpenCLCGenContext getGlobalContext();

    FunctionVariant getVariant();

    String nextVarName();

    ValuesMethodDef newVar(AstNode node);

    ValuesMethodDefF64 newVarF64(AstNode node);

    ValuesMethodDefF32 newVarF32(AstNode node);

    String newVarUnoptimized(AstNode node);

    String getDelegateVar(ValuesMethodDef target, AstNode.ReturnType returnType);

    String getDelegateVar(ValuesMethodDefF64 target);

    String getDelegateVar(ValuesMethodDefF32 target);

    OpenCLCGenFunctionContext fork();

    String getBody();

    void appendRaw(String raw);

    public enum FunctionVariant {
        UNCACHED("_uncached", true, false, false, false),
        FLATCACHE_ONLY("_flatcache_only", true, true, false, false),
        FULLY_CACHED("_fully_cached", true, true, true, false),
        FULLY_CACHED_EXCEPT_CACHE2D("_fully_cached_except_cache2d", false, true, true, false)
        ;

        public final String suffix;
        public final boolean inDispatcher;
        public final boolean enableFlatCache;
        public final boolean enableAllCache;
        private final boolean disableCache2d;

        FunctionVariant(String suffix, boolean inDispatcher, boolean enableFlatCache, boolean enableAllCache, boolean disableCache2d) {
            this.suffix = suffix;
            this.inDispatcher = inDispatcher;
            this.enableFlatCache = enableFlatCache;
            this.enableAllCache = enableAllCache;
            this.disableCache2d = disableCache2d;
        }

        public boolean useCache2D() {
            return this.enableAllCache && !this.disableCache2d;
        }
    }
}
