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

package com.ishland.c2me.threading.lighting.mixin.scalablelux;

import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.common.scheduler.LockTokenImpl;
import com.ishland.c2me.base.common.scheduler.SimplePrioritizedTask;
import com.ishland.flowsched.executor.LockToken;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.ArrayList;

@Pseudo
@Mixin(targets = "ca.spottedleaf.starlight.common.thread.SchedulingUtil")
public class MixinSchedulingUtil {

    /**
     * @author ishland
     * @reason merge thread pool
     */
    @Overwrite(remap = false)
    public static void scheduleTask(int ownerTag, Runnable task, int x, int z, int radius) {
        final ArrayList<LockToken> lockTokens = new ArrayList<>((radius * 2 + 1) * (radius * 2 + 1));
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                lockTokens.add(new LockTokenImpl(ownerTag, ChunkPos.toLong(x + i, z + j), LockTokenImpl.Usage.LIGHTING));
            }
        }
        final SimplePrioritizedTask simpleTask = new SimplePrioritizedTask(task, lockTokens.toArray(LockToken[]::new));
        GlobalExecutors.prioritizedScheduler.schedule(simpleTask, 17);
    }

    /**
     * @author ishlabd
     * @reason merge thread pool
     */
    @Overwrite(remap = false)
    public static boolean isExternallyManaged() {
        return true;
    }

}
