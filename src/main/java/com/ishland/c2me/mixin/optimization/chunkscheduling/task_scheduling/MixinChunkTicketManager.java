package com.ishland.c2me.mixin.optimization.chunkscheduling.task_scheduling;

import com.ishland.c2me.common.optimization.chunkscheduling.IThreadedAnvilChunkStorage;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.Executor;

@Mixin(ChunkTicketManager.class)
public class MixinChunkTicketManager {

    @Dynamic
    @ModifyArg(method = "method_15891", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;tick(Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;Ljava/util/concurrent/Executor;)V"), index = 1) // TODO lambda expression in tick
    private Executor modifyExecutor(ThreadedAnvilChunkStorage chunkStorage, Executor executor) {
        return ((IThreadedAnvilChunkStorage) chunkStorage).getMainInvokingExecutor();
    }

}
