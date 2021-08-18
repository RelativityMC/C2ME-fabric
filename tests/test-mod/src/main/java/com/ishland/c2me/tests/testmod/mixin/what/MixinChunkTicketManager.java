package com.ishland.c2me.tests.testmod.mixin.what;

import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;

@Mixin(ChunkTicketManager.class)
public class MixinChunkTicketManager {

    @Shadow @Final private Executor mainThreadExecutor;

    @Dynamic
    @Inject(method = "*", at = @At("RETURN"), require = 0)
    private void beforeCall(CallbackInfo info) {
        if (this.mainThreadExecutor == null) return;
        if (!((ThreadExecutor) this.mainThreadExecutor).isOnThread()) throw new IllegalStateException("Attempted to update chunk ticket offthread");
    }

}
