package org.yatopiamc.c2me.mixin.threading.chunkio;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerChunkManager.MainThreadExecutor.class)
public abstract class MixinServerChunkManagerMainThreadExecutor extends ThreadExecutor<Runnable> {

    protected MixinServerChunkManagerMainThreadExecutor(String name) {
        super(name);
    }

    @Inject(method = "runTask", at = @At("RETURN"))
    private void onPostRunTask(CallbackInfoReturnable<Boolean> cir) {
        super.runTask();
    }

}
