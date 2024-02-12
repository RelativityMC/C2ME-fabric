package com.ishland.c2me.fixes.general.threading_issues.mixin.asynccatchers;

import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ConcurrentModificationException;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {

    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

    @Inject(method = "loadEntity", at = @At("HEAD"))
    private void preventAsyncEntityLoad(CallbackInfo ci) {
        if (!this.mainThreadExecutor.isOnThread()) {
            final ConcurrentModificationException e = new ConcurrentModificationException("Async entity load");
            e.printStackTrace();
            throw e;
        }
    }

    @Inject(method = "unloadEntity", at = @At("HEAD"))
    private void preventAsyncEntityUnload(CallbackInfo ci) {
        if (!this.mainThreadExecutor.isOnThread()) {
            final ConcurrentModificationException e = new ConcurrentModificationException("Async entity unload");
            e.printStackTrace();
            throw e;
        }
    }

}
