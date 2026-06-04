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

package com.ishland.c2me.notickvd.common;

import com.ishland.c2me.base.common.GlobalExecutors;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class NoTickSystem {

    private final PlayerNoTickLoader playerNoTickLoader;
    private final ConcurrentLinkedQueue<Runnable> pendingActionsOnScheduler = new ConcurrentLinkedQueue<>();
    final ConcurrentLinkedQueue<Runnable> mainBeforeTicketTicks = new ConcurrentLinkedQueue<>();
    final ConcurrentLinkedQueue<Runnable> mainAfterTicketTicks = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean isTicking = new AtomicBoolean();
    final Executor executor = GlobalExecutors.asyncScheduler;

    public NoTickSystem(ServerChunkLoadingManager tacs) {
        this.playerNoTickLoader = new PlayerNoTickLoader(tacs, this);
    }

    public void addPlayerSource(ChunkPos chunkPos) {
        this.pendingActionsOnScheduler.add(() -> this.playerNoTickLoader.addSource(chunkPos));
    }

    public void removePlayerSource(ChunkPos chunkPos) {
        this.pendingActionsOnScheduler.add(() -> this.playerNoTickLoader.removeSource(chunkPos));
    }

    public void setNoTickViewDistance(int viewDistance) {
        this.pendingActionsOnScheduler.add(() -> this.playerNoTickLoader.setViewDistance(viewDistance));
    }

    public void beforeTicketTicks() {
        drainQueue(this.mainBeforeTicketTicks);
    }

    public void afterTicketTicks() {
        drainQueue(this.mainAfterTicketTicks);
    }

    public void tick() {
        scheduleTick();
    }

    private void scheduleTick() {
        if (!this.pendingActionsOnScheduler.isEmpty() && this.isTicking.compareAndSet(false, true)) {
            List<Runnable> tasks = new ArrayList<>(this.pendingActionsOnScheduler.size() + 3);
            {
                Runnable r;
                while ((r = this.pendingActionsOnScheduler.poll()) != null) {
                    tasks.add(r);
                }
            }
            executor.execute(() -> {
                try {
                    for (Runnable task : tasks) {
                        try {
                            task.run();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }

                    this.playerNoTickLoader.tick();
                    if (!this.pendingActionsOnScheduler.isEmpty() || !tasks.isEmpty()) scheduleTick(); // run more tasks
                } finally {
                    this.isTicking.set(false);
                }
            });
        }
    }

    private void drainQueue(ConcurrentLinkedQueue<Runnable> queue) {
        Runnable runnable;
        while ((runnable = queue.poll()) != null) {
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public long getPendingLoadsCount() {
        return this.playerNoTickLoader.getPendingLoadsCount();
    }

    public void close() {
        this.playerNoTickLoader.close();
        executor.execute(() -> {
            try {
                this.playerNoTickLoader.tick();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
