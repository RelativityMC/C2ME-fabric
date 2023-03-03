package com.ishland.c2me.opts.scheduling.mixin.task_scheduling;

import com.ishland.c2me.opts.scheduling.common.IThreadedAnvilChunkStorage;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage implements IThreadedAnvilChunkStorage {

    @Shadow
    @Final
    private ServerWorld world;
    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

    @Shadow @Final private ThreadedAnvilChunkStorage.TicketManager ticketManager;
    private final Executor mainInvokingExecutor = runnable -> {
        if (this.world.getServer().isOnThread()) {
            runnable.run();
        } else {
            this.mainThreadExecutor.execute(runnable);
        }
    };

    @Override
    public Executor getMainInvokingExecutor() {
        return mainInvokingExecutor;
    }

    /**
     * reduce scheduling overhead with mainInvokingExecutor
     */
    @Redirect(method = "makeChunkEntitiesTickable", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private <U, T> CompletableFuture<U> redirectMainThreadExecutor1(CompletableFuture<T> completableFuture, Function<? super T, ? extends U> fn, Executor executor) {
        return completableFuture.thenApplyAsync(fn, this.mainInvokingExecutor);
    }

//    /**
//     * @author ishland
//     * @reason reduce scheduling overhead with mainInvokingExecutor
//     */
//    @Overwrite
//    public void releaseLightTicket(ChunkPos pos) {
//        // TODO [VanilaCopy]
//        this.mainInvokingExecutor.execute(Util.debugRunnable(() -> {
//            this.ticketManager.removeTicketWithLevel(ChunkTicketType.LIGHT, pos, 33 + ChunkStatus.getDistanceFromFull(ChunkStatus.LIGHT), pos);
//        }, () -> {
//            return "release light ticket " + pos;
//        }));
//    }

    // private synthetic method_17252(Lnet/minecraft/server/world/ChunkHolder;Ljava/lang/Runnable;)V
    /**
     * TODO lambda expression of convertToFullChunk
     *
     * @author ishland
     * @reason reduce scheduling overhead with mainInvokingExecutor
     */
    @SuppressWarnings("OverwriteTarget")
    @Dynamic
    @Overwrite
    private void method_17252(ChunkHolder holder, Runnable runnable) {
        this.mainInvokingExecutor.execute(runnable);
    }

    // private synthetic method_19487(Lnet/minecraft/server/world/ChunkHolder;Ljava/lang/Runnable;)V
    /**
     * TODO first lambda expression of makeChunkTickable
     *
     * @author ishland
     * @reason reduce scheduling overhead with mainInvokingExecutor
     */
    @SuppressWarnings("OverwriteTarget")
    @Dynamic
    @Overwrite
    private void method_19487(ChunkHolder holder, Runnable runnable) {
        this.mainInvokingExecutor.execute(runnable);
    }

    // private synthetic method_19486(Lnet/minecraft/server/world/ChunkHolder;Ljava/lang/Runnable;)V
    /**
     * TODO second lambda expression of makeChunkTickable
     *
     * @author ishland
     * @reason reduce scheduling overhead with mainInvokingExecutor
     */
    @SuppressWarnings("OverwriteTarget")
    @Dynamic
    @Overwrite
    private void method_19486(ChunkHolder holder, Runnable runnable) {
        this.mainInvokingExecutor.execute(runnable);
    }

    // private synthetic method_20579(Lnet/minecraft/server/world/ChunkHolder;Ljava/lang/Runnable;)V
    /**
     * TODO lambda expression of method_31417 (makeChunkAccessible)
     *
     * @author ishland
     * @reason reduce scheduling overhead with mainInvokingExecutor
     */
    @SuppressWarnings("OverwriteTarget")
    @Dynamic
    @Overwrite
    private void method_20579(ChunkHolder holder, Runnable runnable) {
        this.mainInvokingExecutor.execute(runnable);
    }

}
