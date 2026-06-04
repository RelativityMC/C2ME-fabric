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
