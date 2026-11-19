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

package com.ishland.c2me.notickvd.mixin.client;

import com.mojang.datafixers.DataFixer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.dedicated.management.listener.CompositeManagementListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ApiServices;
import net.minecraft.world.chunk.ChunkLoadProgress;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.rule.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.Optional;

@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer extends MinecraftServer {

    public MixinIntegratedServer(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Optional<GameRules> gameRules, Proxy proxy, DataFixer fixerUpper, ApiServices apiServices, ChunkLoadProgress chunkLoadProgress, boolean propagatesCrashes, CompositeManagementListener compositeManagementListener) {
        super(serverThread, session, dataPackManager, saveLoader, gameRules, proxy, fixerUpper, apiServices, chunkLoadProgress, propagatesCrashes, compositeManagementListener);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServer;incrementTotalWorldTimeStat()V", shift = At.Shift.AFTER))
    private void afterPauseLoop(CallbackInfo ci) {
        for(ServerPlayerEntity serverPlayerEntity : this.getPlayerManager().getPlayerList()) {
            serverPlayerEntity.networkHandler.disableFlush();
            serverPlayerEntity.networkHandler.chunkDataSender.sendChunkBatches(serverPlayerEntity);
            serverPlayerEntity.networkHandler.enableFlush();
        }
    }

}
