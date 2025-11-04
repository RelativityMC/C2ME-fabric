package com.ishland.c2me.base.mixin.bugfixes.config_enforce_main;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ServerConfigurationPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.config.AcceptCodeOfConductC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class MixinServerConfigurationNetworkHandler extends ServerCommonNetworkHandler implements ServerConfigurationPacketListener, TickablePacketListener {

    public MixinServerConfigurationNetworkHandler(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "onResourcePackStatus", at = @At("HEAD"))
    private void onResourcePackStatus(ResourcePackStatusC2SPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, this, this.server.getPacketApplyBatcher());
    }

    @Inject(method = "onAcceptCodeOfConduct", at = @At("HEAD"))
    private void onAcceptCodeOfConduct(AcceptCodeOfConductC2SPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, this, this.server.getPacketApplyBatcher());
    }

}
