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

package com.ishland.c2me.opts.dfc.common.ast;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.lang.invoke.VarHandle;

public class FrontendRegistry<E extends AstEmitter<? extends DensityFunction>> {

    private final Reference2ReferenceOpenHashMap<Class<? extends DensityFunction>, E> exactMatches = new Reference2ReferenceOpenHashMap<>();
    private volatile boolean frozen = false;

    public <N extends DensityFunction, E1 extends AstEmitter<N>> void registerExactMatch(Class<N> clazz, E1 emitter) {
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

    public <N extends DensityFunction, E1 extends AstEmitter<N>> E1 getOptional(Class<N> clazz) {
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

        return null;
    }

}
