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

package com.ishland.c2me.notickvd.mixin;

import com.ishland.c2me.base.common.theinterface.IFastChunkHolder;
import com.ishland.c2me.base.common.util.FilteringIterable;
import com.ishland.c2me.base.mixin.access.IChunkLevelManager;
import com.ishland.c2me.base.mixin.access.ISimulationDistanceLevelPropagator;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelManager;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkManager.class)
public class MixinServerChunkManager {

    @Shadow @Final public ServerChunkLoadingManager chunkLoadingManager;

    @WrapOperation(method = "tickChunks(Lnet/minecraft/util/profiler/Profiler;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;iterateEntities()Ljava/lang/Iterable;"))
    private Iterable<Entity> redirectIterateEntities(ServerWorld serverWorld, Operation<Iterable<Entity>> op) {
        Long2ByteMap trackedChunks = ((ISimulationDistanceLevelPropagator) ((IChunkLevelManager) ((IThreadedAnvilChunkStorage) this.chunkLoadingManager).getLevelManager()).getSimulationDistanceLevelPropagator()).getLevels();
        return new FilteringIterable<>(op.call(serverWorld), entity -> trackedChunks.containsKey(entity.getChunkPos().toLong()));
    }

    @Redirect(method = "broadcastUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getWorldChunk()Lnet/minecraft/world/chunk/WorldChunk;"))
    private WorldChunk broadcastBorderChunks(ChunkHolder instance) {
        if (instance instanceof IFastChunkHolder fastChunkHolder) {
            return fastChunkHolder.c2me$immediateWorldChunk();
        } else {
            return instance.getAccessibleFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).orElse(null);
        }
    }

}
