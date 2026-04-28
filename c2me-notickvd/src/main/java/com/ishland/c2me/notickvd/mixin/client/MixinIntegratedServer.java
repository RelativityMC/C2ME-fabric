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
