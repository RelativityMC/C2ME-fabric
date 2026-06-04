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

package com.ishland.c2me.base.common.scheduler;

import com.ishland.flowsched.executor.LockToken;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ScheduledTask<T> extends AbstractPosAwarePrioritizedTask {

    private final Supplier<CompletableFuture<T>> action;
    private final LockToken[] lockTokens;
    private final CompletableFuture<T> future = new CompletableFuture<>();

    public ScheduledTask(long pos, Supplier<CompletableFuture<T>> action, LockToken[] lockTokens) {
        super(pos);
        this.action = action;
        this.lockTokens = lockTokens;
    }

    @Override
    public void run(Runnable releaseLocks) {
        action.get().whenComplete((t, throwable) -> {
            releaseLocks.run();
            if (throwable != null) {
                future.completeExceptionally(throwable);
            } else {
                future.complete(t);
            }
            for (Runnable runnable : this.postExec) {
                try {
                    runnable.run();
                } catch (Throwable t1) {
                    t1.printStackTrace();
                }
            }
        });
    }

    @Override
    public void propagateException(Throwable t) {
        future.completeExceptionally(t);
    }

    @Override
    public LockToken[] lockTokens() {
        return this.lockTokens;
    }

    public CompletableFuture<T> getFuture() {
        return this.future;
    }
}
