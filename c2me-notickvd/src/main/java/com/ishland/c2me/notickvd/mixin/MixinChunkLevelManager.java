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

package com.ishland.c2me.notickvd.mixin;

import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorageLevelManager;
import com.ishland.c2me.notickvd.common.ChunkLevelManagerExtension;
import com.ishland.c2me.notickvd.common.NoTickSystem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkLevelManager;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkSectionPos;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkLevelManager.class)
public class MixinChunkLevelManager implements ChunkLevelManagerExtension {

    @Shadow @Final private ChunkLevelManager.NearbyChunkTicketUpdater nearbyChunkTicketUpdater;

    @Shadow
    @Final
    private static Logger LOGGER;
    @Unique
    private NoTickSystem noTickSystem;
    @Unique
    private int c2me$maximumSeenViewDistance;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.noTickSystem = new NoTickSystem(((IThreadedAnvilChunkStorageLevelManager) this).c2me$getSuperClass());
    }

    @Inject(method = "handleChunkEnter", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkLevelManager$DistanceFromNearestPlayerTracker;updateLevel(JIZ)V", shift = At.Shift.AFTER))
    private void onHandleChunkEnter(ChunkSectionPos pos, ServerPlayerEntity player, CallbackInfo ci) {
        this.noTickSystem.addPlayerSource(pos.toChunkPos());
    }

    @Inject(method = "handleChunkLeave", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkLevelManager$DistanceFromNearestPlayerTracker;updateLevel(JIZ)V", shift = At.Shift.AFTER))
    private void onHandleChunkLeave(ChunkSectionPos pos, ServerPlayerEntity player, CallbackInfo ci) {
        this.noTickSystem.removePlayerSource(pos.toChunkPos());
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void beforeTick(ServerChunkLoadingManager chunkStorage, CallbackInfoReturnable<Boolean> cir) {
        this.noTickSystem.beforeTicketTicks();
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void onTick(ServerChunkLoadingManager chunkStorage, CallbackInfoReturnable<Boolean> cir) {
        this.noTickSystem.afterTicketTicks();
        this.noTickSystem.tick();
    }

    @Inject(method = "setSimulationDistance", at = @At("HEAD"))
    public void mapSimulationDistance(int simulationDistance, CallbackInfo ci) {
        this.nearbyChunkTicketUpdater.setWatchDistance(simulationDistance);
    }

    /**
     * @author ishland
     * @reason remap setWatchDistance to no-tick one
     */
    @Overwrite
    public void setWatchDistance(int viewDistance) {
        if (viewDistance > 32) {
            if (viewDistance > this.c2me$maximumSeenViewDistance) {
                LOGGER.warn("Setting server view distance to {} anyways, which is bigger than what vanilla allows. This have performance implications so please make sure this isn't a misconfiguration. ", viewDistance);
                this.c2me$maximumSeenViewDistance = viewDistance;
            }
        }
        this.noTickSystem.setNoTickViewDistance(viewDistance + 1);
    }

    @Override
    @Unique
    public long c2me$getPendingLoadsCount() {
        return this.noTickSystem.getPendingLoadsCount();
    }

    @Override
    public void c2me$closeNoTickVD() {
        this.noTickSystem.close();
    }
}
