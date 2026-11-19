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

package com.ishland.c2me.opts.dfc.common.gen;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import java.lang.invoke.VarHandle;

public class CodeGenRegistry<E extends CodeEmitter<? extends AstNode>> {

    private final Reference2ReferenceOpenHashMap<Class<? extends AstNode>, E> exactMatches = new Reference2ReferenceOpenHashMap<>();
    private volatile boolean frozen = false;

    public <N extends AstNode, E1 extends CodeEmitter<N>> void registerExactMatch(Class<N> clazz, E1 emitter) {
        if (!this.frozen) {
            synchronized (this) {
                if (!this.frozen) {
                    VarHandle.fullFence();
                    if (this.exactMatches.containsKey(clazz)) {
                        throw new IllegalArgumentException("Already registered");
                    }
                    this.exactMatches.put(clazz, (E) emitter);
                    VarHandle.fullFence();
                    return;
                }
            }
        }

        throw new IllegalStateException("Already frozen");
    }

    public <N extends AstNode, E1 extends CodeEmitter<N>> E1 get(Class<N> clazz) {
        if (!this.frozen) {
            synchronized (this) {
                if (!this.frozen) {
                    this.frozen = true;
                }
            }
            VarHandle.fullFence();
        }

        E exactMatch = this.exactMatches.get(clazz);
        if (exactMatch != null) {
            return (E1) exactMatch;
        }

        throw new UnsupportedOperationException("Unknown class %s".formatted(clazz));
    }

}
