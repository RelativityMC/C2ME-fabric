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

package com.ishland.c2me.rewrites.chunksystem.common.structs;

import com.ishland.c2me.base.common.GlobalExecutors;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

public class ChunkSystemExecutors {

    public static final ScopedValue<Queue<Runnable>> CONSOLIDATING_QUEUE = ScopedValue.newInstance();

    public static final Executor backingBackgroundExecutor = GlobalExecutors.prioritizedScheduler.executor(15);
    public static final Scheduler backgroundScheduler = Schedulers.from(backingBackgroundExecutor);
    public static final Executor consolidatingBackgroundExecutor = command -> {
        if (!CONSOLIDATING_QUEUE.isBound()) { // first entry
            consolidatingRoot(command);
        } else {
            Queue<Runnable> runnables = CONSOLIDATING_QUEUE.get();
            runnables.add(command);
        }
    };
    public static final Scheduler consolidatingBackgroundScheduler = Schedulers.from(consolidatingBackgroundExecutor);

    private static void consolidatingRoot(Runnable initialCommand) {
        ArrayDeque<Runnable> runnables = new ArrayDeque<>();
        runnables.add(initialCommand);
        consolidatingRoot0(runnables);
    }

    public static void consolidatingRoot0(ArrayDeque<Runnable> runnables) {
        backingBackgroundExecutor.execute(() -> {
            ScopedValue
                    .where(CONSOLIDATING_QUEUE, runnables)
                    .run(() -> {
                        while (!runnables.isEmpty()) {
                            try {
                                runnables.remove().run();
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                    });
        });
    }

}
