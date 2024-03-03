package com.ishland.c2me.threading.worldgen.mixin;

import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.threading.worldgen.common.Config;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;

@Mixin(value = ChunkHolder.class, priority = 1110)
public abstract class MixinChunkHolder {

    @Shadow public abstract CompletableFuture<OptionalChunk<Chunk>> getChunkAt(ChunkStatus targetStatus, ThreadedAnvilChunkStorage chunkStorage);

    @Redirect(method = "getChunkAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;getChunk(Lnet/minecraft/server/world/ChunkHolder;Lnet/minecraft/world/chunk/ChunkStatus;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<OptionalChunk<Chunk>> redirectGetChunk(ThreadedAnvilChunkStorage instance, ChunkHolder holder, ChunkStatus requiredStatus) {
        if (requiredStatus == ChunkStatus.EMPTY) {
            return instance.getChunk(holder, requiredStatus);
        } else {
            return this.getChunkAt(requiredStatus.getPrevious(), instance)
                    .thenComposeAsync(
                            unused -> instance.getChunk(holder, requiredStatus),
                            Config.asyncScheduling ? Runnable::run : r -> {
                                final ThreadExecutor<Runnable> executor = ((IThreadedAnvilChunkStorage) instance).getMainThreadExecutor();
                                if (executor.isOnThread()) {
                                    r.run();
                                } else {
                                    executor.execute(r);
                                }
                            }
                    );
        }
    }

}
