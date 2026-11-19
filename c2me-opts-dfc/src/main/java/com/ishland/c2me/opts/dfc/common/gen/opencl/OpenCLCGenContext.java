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
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;

public interface OpenCLCGenContext {
    String signature = "(const sample_int32_ctx_t ctx)";
    String filler = "nop();".repeat(200) + "\n";

    String nextMethodName();

    String nextMethodName(String suffix);

    ValuesMethodDef newDispatcher(AstNode node, String id, AstNode.ReturnType returnType);

    ValuesMethodDefF64 newDispatcherF64(AstNode node);

    ValuesMethodDefF64 newDispatcherF64(AstNode node, String id);

    ValuesMethodDefF32 newDispatcherF32(AstNode node);

    ValuesMethodDefF32 newDispatcherF32(AstNode node, String id);

    ValuesMethodDef newMethod(AstNode node, OpenCLCGenFunctionContext.FunctionVariant variant, AstNode.ReturnType returnType);

    ValuesMethodDefF64 newMethodF64(AstNode node, OpenCLCGenFunctionContext.FunctionVariant variant);

    ValuesMethodDefF32 newMethodF32(AstNode node, OpenCLCGenFunctionContext.FunctionVariant variant);

    String callDelegate(ValuesMethodDef target, AstNode.ReturnType returnType);

    String callDelegate(ValuesMethodDefF64 target);

    String callDelegate(ValuesMethodDefF32 target);

    int allocGlobalDynamicData(Object data);

    int allocGlobalConstData(byte[] data, int alignment);

    int allocGlobalConstDataObject(Object obj);

    int getGlobalDynamicDataOffset(Object data);

    int registerFlatCache(CacheLikeNode node);

    int registerCache2d(CacheLikeNode node);

    int registerInterpolator(CacheLikeNode node);

    void appendRaw(String raw);
}
