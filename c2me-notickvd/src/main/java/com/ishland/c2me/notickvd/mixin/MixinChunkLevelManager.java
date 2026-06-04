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

import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorageLevelManager;
import com.ishland.c2me.notickvd.common.ChunkLevelManagerExtension;
import com.ishland.c2me.notickvd.common.NoTickSystem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkLevelManager;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkSectionPos;
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

    @Unique
    private NoTickSystem noTickSystem;

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
