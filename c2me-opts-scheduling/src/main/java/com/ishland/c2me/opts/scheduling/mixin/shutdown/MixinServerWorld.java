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

package com.ishland.c2me.opts.scheduling.mixin.shutdown;

import com.ishland.c2me.base.mixin.access.IThreadExecutor;
import com.ishland.c2me.opts.scheduling.common.ITryFlushable;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.locks.LockSupport;

@Mixin(ServerWorld.class)
public class MixinServerWorld {

    @Shadow @Final private MinecraftServer server;

    @Shadow @Final private ServerChunkManager chunkManager;

    @Shadow @Final private ServerEntityManager<Entity> entityManager;

    @Inject(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerEntityManager;flush()V", shift = At.Shift.BEFORE))
    private void replaceEntityFlushLogic(ProgressListener progressListener, boolean flush, boolean savingDisabled, CallbackInfo ci) {
        while (!((ITryFlushable) this.entityManager).c2me$tryFlush()) {
            ((IThreadExecutor) this.server).invokeRunTask();
            while (this.chunkManager.executeQueuedTasks());
            LockSupport.parkNanos("waiting for completion", 1_000_000);
        }
    }

}
