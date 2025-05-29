package com.ishland.c2me.base.mixin.client_movement;

import com.ishland.c2me.base.common.theinterface.PlayerEntityExtension;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerLoadedC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity implements PlayerEntityExtension {

    @Shadow @Final public ClientPlayNetworkHandler networkHandler;

    @Override
    public void c2me$onForcedLoaded() {
        this.networkHandler.sendPacket(new PlayerLoadedC2SPacket());
    }

}
