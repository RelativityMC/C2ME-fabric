package com.ishland.c2me.opts.scheduling.mixin.task_scheduling;

import com.ishland.c2me.base.mixin.access.IServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.storage.EntityChunkDataAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.Executor;

@Mixin(EntityChunkDataAccess.class)
public class MixinEntityChunkDataAccess {

    @Shadow @Final private ServerWorld world;

    @ModifyArg(method = "readChunkData", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private Executor redirectExecutor(Executor executor) {
        return ((IServerChunkManager) this.world.getChunkManager()).getMainThreadExecutor();
    }

}
