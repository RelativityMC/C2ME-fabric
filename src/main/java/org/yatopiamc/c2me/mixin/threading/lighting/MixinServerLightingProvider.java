package org.yatopiamc.c2me.mixin.threading.lighting;

import net.minecraft.server.world.ServerLightingProvider;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLightingProvider.class)
public abstract class MixinServerLightingProvider {

    @Shadow public abstract void tick();

    @Dynamic
    @Inject(method = "method_19505", at = @At("RETURN"))
    private void onPostRunTask(CallbackInfo info) {
        this.tick(); // Run more tasks
    }

}
