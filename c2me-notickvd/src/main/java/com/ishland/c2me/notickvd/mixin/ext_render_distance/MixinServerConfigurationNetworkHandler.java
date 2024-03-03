package com.ishland.c2me.notickvd.mixin.ext_render_distance;

import com.ishland.c2me.base.mixin.access.ISyncedClientOptions;
import com.ishland.c2me.notickvd.common.IRenderDistanceOverride;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerConfigurationNetworkHandler.class)
public class MixinServerConfigurationNetworkHandler implements IRenderDistanceOverride {

    @Shadow private SyncedClientOptions syncedOptions;
    @Unique
    private boolean c2me_notickvd$hasRenderDistanceOverride = false;

    @Override
    public void c2me_notickvd$setRenderDistance(int renderDistance) {
        this.c2me_notickvd$hasRenderDistanceOverride = true;
        ((ISyncedClientOptions) (Object) this.syncedOptions).setViewDistance(renderDistance);
    }

    @WrapOperation(method = "onClientOptions", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerConfigurationNetworkHandler;syncedOptions:Lnet/minecraft/network/packet/c2s/common/SyncedClientOptions;", opcode = Opcodes.PUTFIELD))
    private void interceptClientOptions(ServerConfigurationNetworkHandler instance, SyncedClientOptions value, Operation<Void> original) {
        if (c2me_notickvd$hasRenderDistanceOverride) {
            ((ISyncedClientOptions) (Object) value).setViewDistance(this.syncedOptions.viewDistance()); // keep the original view distance
        }
        original.call(instance, value);
    }

}
