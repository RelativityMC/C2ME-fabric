package com.ishland.c2me.threading.lighting.mixin;

import net.minecraft.server.world.ServerLightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLightingProvider.class)
public abstract class MixinServerLightingProvider {

    @Shadow public abstract void tick();

//    @Dynamic
//    @Inject(method = "method_19505", at = @At("RETURN"))
//    private void onPostRunTask(CallbackInfo info) {
//        this.tick(); // Run more tasks
//    }

}
