package com.ishland.c2me.fixes.chunkio.threading_issues.mixin;

import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;

@Mixin(ChunkHolder.class)
public class MixinChunkHolder {

    @Inject(method = "updateFutures", at = @At("HEAD"))
    private void beforeTick(ThreadedAnvilChunkStorage chunkStorage, Executor executor, CallbackInfo ci) {
        ((IThreadedAnvilChunkStorage) chunkStorage).invokeUpdateHolderMap();
    }

}
