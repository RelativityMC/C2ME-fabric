package com.ishland.c2me.fixes.general.threading_issues.mixin.asynccatchers;

import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ConcurrentModificationException;

@Mixin(ServerChunkManager.class)
public class MixinServerChunkManager {

    @Shadow @Final private Thread serverThread;

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;Z)V", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (Thread.currentThread() != this.serverThread) {
            final ConcurrentModificationException e = new ConcurrentModificationException("Async ticking server chunk manager");
            e.printStackTrace();
            throw e;
        }
    }

}
