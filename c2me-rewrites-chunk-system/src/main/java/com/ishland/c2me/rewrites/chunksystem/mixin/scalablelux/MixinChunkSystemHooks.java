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

package com.ishland.c2me.rewrites.chunksystem.mixin.scalablelux;

import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.c2me.rewrites.chunksystem.common.TheChunkSystem;
import com.ishland.c2me.rewrites.chunksystem.common.TicketTypeExtension;
import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.AsyncSerializationUtil;
import com.ishland.c2me.rewrites.chunksystem.common.ducks.IChunkSystemAccess;
import com.ishland.flowsched.scheduler.StatusAdvancingScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "ca.spottedleaf.starlight.common.integration.v0.ChunkSystemHooks")
public class MixinChunkSystemHooks {

    /**
     * @author ishland
     * @reason implementation
     */
    @Overwrite
    public static boolean isTicketThreadSafe() {
        return true;
    }

    /**
     * @author ishland
     * @reason implementation
     */
    @Overwrite
    public static boolean isNonFullTicket() {
        return true;
    }

    /**
     * @author ishland
     * @reason implementation
     */
    @Overwrite
    public static boolean avoidLightCopy() {
        return AsyncSerializationUtil.duringUnloadSerialization.isBound();
    }

    /**
     * @author ishland
     * @reason implementation
     */
    @Overwrite
    public static void addLightTicket(ServerWorld world, ChunkPos pos) {
        TheChunkSystem theChunkSystem = ((IChunkSystemAccess) world.getChunkManager().chunkLoadingManager).c2me$getTheChunkSystem();
        theChunkSystem.addTicket(pos, TicketTypeExtension.LIGHT_TICKET, pos, NewChunkStatus.fromVanillaStatus(ChunkStatus.LIGHT), null);
    }

    /**
     * @author ishland
     * @reason implementation
     */
    @Overwrite
    public static void removeLightTicket(ServerWorld world, ChunkPos pos) {
        TheChunkSystem theChunkSystem = ((IChunkSystemAccess) world.getChunkManager().chunkLoadingManager).c2me$getTheChunkSystem();
        theChunkSystem.removeTicket(pos, TicketTypeExtension.LIGHT_TICKET, pos, NewChunkStatus.fromVanillaStatus(ChunkStatus.LIGHT));
    }

}
