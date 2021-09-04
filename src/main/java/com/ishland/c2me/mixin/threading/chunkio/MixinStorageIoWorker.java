package com.ishland.c2me.mixin.threading.chunkio;

import com.ishland.c2me.common.threading.chunkio.ChunkIoThreadingExecutorUtils;
import com.ishland.c2me.common.threading.chunkio.IAsyncChunkStorage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageIoWorker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

@Mixin(StorageIoWorker.class)
public abstract class MixinStorageIoWorker implements IAsyncChunkStorage {

    @Shadow protected abstract CompletableFuture<NbtCompound> readChunkData(ChunkPos pos);

    private ExecutorService threadExecutor;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getIoWorkerExecutor()Ljava/util/concurrent/Executor;"))
    private Executor redirectIoWorkerExecutor() {
        return threadExecutor = Executors.newSingleThreadExecutor(ChunkIoThreadingExecutorUtils.ioWorkerFactory);
    }

    @Override
    public CompletableFuture<NbtCompound> getNbtAtAsync(ChunkPos pos) {
        return readChunkData(pos);
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/thread/TaskExecutor;close()V", shift = At.Shift.AFTER))
    private void onClose(CallbackInfo ci) {
        threadExecutor.shutdown();
        while (!threadExecutor.isTerminated()) {
            LockSupport.parkNanos("Waiting for thread executor termination", 100_000);
        }
    }
}
