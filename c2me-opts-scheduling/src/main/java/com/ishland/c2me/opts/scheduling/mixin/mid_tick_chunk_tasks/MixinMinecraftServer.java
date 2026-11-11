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

package com.ishland.c2me.opts.scheduling.mixin.mid_tick_chunk_tasks;

import com.ishland.c2me.base.mixin.access.IServerChunkManager;
import com.ishland.c2me.base.mixin.access.IThreadExecutor;
import com.ishland.c2me.opts.scheduling.common.Config;
import com.ishland.c2me.opts.scheduling.common.ServerMidTickTask;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements ServerMidTickTask {

    @Shadow public abstract Iterable<ServerWorld> getWorlds();

    @Shadow @Final private Thread serverThread;
    @Unique
    private long midTickChunkTasksLastRun = System.nanoTime();

    public void executeTasksMidTick(ServerWorld world) {
        if (this.serverThread != Thread.currentThread()) return;
        if (System.nanoTime() - midTickChunkTasksLastRun < Config.midTickChunkTasksInterval) return;
        ((IThreadExecutor) ((ThreadExecutor<Runnable>) ((IServerChunkManager) world.getChunkManager()).getMainThreadExecutor())).invokeRunTask();
        midTickChunkTasksLastRun = System.nanoTime();
    }

}
