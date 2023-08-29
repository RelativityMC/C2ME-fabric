package com.ishland.c2me.threading.lighting.mixin.starlight;

import net.minecraft.server.world.ServerLightingProvider;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mixin(ServerLightingProvider.class)
public class MixinServerLightingProvider {

    @Dynamic
    @Inject(method = "queueTaskForSection(IIILjava/util/function/Supplier;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/thread/ThreadExecutor;isOnThread()Z", remap = true), remap = false, cancellable = true)
    private void forceExecuteImmediately(int chunkX, int chunkY, int chunkZ, Supplier<CompletableFuture<Void>> runnable, CallbackInfo ci) {
        runnable.get();
        ci.cancel();
    }

}
