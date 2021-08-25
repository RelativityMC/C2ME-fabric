package com.ishland.c2me.mixin.optimization.chunkscheduling.task_scheduling;

import com.ishland.c2me.mixin.access.IChunkHolder;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@Mixin(ChunkTicketManager.class)
public class MixinChunkTicketManager {

    @Shadow @Final private Executor mainThreadExecutor;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Set;forEach(Ljava/util/function/Consumer;)V"))
    private void onChunkHolderForEach(Set<ChunkHolder> set, Consumer<ChunkHolder> action, ThreadedAnvilChunkStorage threadedAnvilChunkStorage) {
        if (set.size() < 32) {
            for (ChunkHolder chunkHolder : set) {
                ((IChunkHolder) chunkHolder).invokeTick(threadedAnvilChunkStorage, this.mainThreadExecutor);
            }

        }
        ArrayList<Runnable> pendingTasks = new ArrayList<>((int) (set.size() * 1.1));
        Executor executor = pendingTasks::add;
        try {
            for (ChunkHolder chunkHolder : set) {
                ((IChunkHolder) chunkHolder).invokeTick(threadedAnvilChunkStorage, executor);
            }
        } finally {
            this.mainThreadExecutor.execute(() -> {
                for (Runnable pendingTask : pendingTasks) {
                    try {
                        pendingTask.run();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            });
        }

    }

}
