package com.ishland.c2me.threading.chunkio.mixin;

import com.ishland.c2me.threading.chunkio.common.ChunkIoMainThreadTaskUtils;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerChunkManager.class)
public class MixinServerChunkManager {

    @Inject(method = "executeQueuedTasks", at = @At(value = "RETURN"))
    private void onExecuteTasks(CallbackInfoReturnable<Boolean> cir) {
        ChunkIoMainThreadTaskUtils.drainQueue();
    }

}
