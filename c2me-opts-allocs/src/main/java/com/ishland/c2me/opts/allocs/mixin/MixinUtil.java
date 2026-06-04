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
