package com.ishland.c2me.base.mixin.client_movement;

import com.ishland.c2me.base.common.theinterface.PlayerEntityExtension;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity implements PlayerEntityExtension {

    @Shadow private boolean loaded;

    @Shadow protected int remainingLoadTicks;

    @Override
    public void c2me$onForcedLoaded() {
        // no-op, intended to be overridden
    }

    @Inject(method = "tickLoaded", at = @At("RETURN"))
    private void onForcedLoaded(CallbackInfo ci) {
        if (!this.loaded && this.remainingLoadTicks == 0) {
            this.c2me$onForcedLoaded();
        }
    }

}
