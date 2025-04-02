package com.ishland.c2me.notickvd.mixin.client;

import com.mojang.datafixers.DataFixer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ApiServices;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer extends MinecraftServer {

    public MixinIntegratedServer(Thread serverThread, LevelStorage.Session session, Proxy proxy, DataFixer dataFixer, ApiServices apiServices) {
        super(serverThread, session, proxy, dataFixer, apiServices);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_10961;method_68954()V", shift = At.Shift.AFTER))
    private void afterPauseLoop(CallbackInfo ci) {
        for(ServerPlayerEntity serverPlayerEntity : this.field_59588.method_68990().getPlayerList()) {
            serverPlayerEntity.networkHandler.disableFlush();
            serverPlayerEntity.networkHandler.chunkDataSender.sendChunkBatches(serverPlayerEntity);
            serverPlayerEntity.networkHandler.enableFlush();
        }
    }

}
