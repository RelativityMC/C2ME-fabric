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

package com.ishland.c2me.opts.allocs.mixin;

import com.ibm.asyncutil.util.Combinators;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Mixin(Util.class)
public abstract class MixinUtil {

    /**
     * @author ishland
     * @reason use another impl
     */
    @Overwrite
    public static <V> CompletableFuture<List<V>> combineSafe(List<CompletableFuture<V>> futures) {
        return Combinators.collect(futures, Collectors.toList()).toCompletableFuture();
    }

    /**
     * @author ishland
     * @reason use another impl
     */
    @Overwrite
    public static <V> CompletableFuture<List<V>> combine(List<CompletableFuture<V>> futures) {
        final CompletableFuture<List<V>> future = Combinators.collect(futures, Collectors.toList()).toCompletableFuture();
        BiConsumer<V, Throwable> action = (v, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
            }
        };
        for (CompletableFuture<V> completableFuture : futures) {
            completableFuture.whenComplete(action);
        }
        return future;
    }

    /**
     * @author ishland
     * @reason use another impl
     */
    @Overwrite
    public static <V> CompletableFuture<List<V>> combineCancellable(List<CompletableFuture<V>> futures) {
        final CompletableFuture<List<V>> future = Combinators.collect(futures, Collectors.toList()).toCompletableFuture();
        BiConsumer<V, Throwable> action = (v, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                futures.forEach(f -> f.cancel(false));
            }
        };
        for (CompletableFuture<V> completableFuture : futures) {
            completableFuture.whenComplete(action);
        }
        return future;
    }

}
