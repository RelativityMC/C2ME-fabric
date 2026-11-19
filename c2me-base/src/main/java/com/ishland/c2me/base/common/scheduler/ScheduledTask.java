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
