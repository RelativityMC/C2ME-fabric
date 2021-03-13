package org.yatopiamc.c2me.mixin.threading.chunkio;

import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.storage.EntityChunkDataAccess;
import net.minecraft.world.storage.StorageIoWorker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.yatopiamc.c2me.common.threading.chunkio.C2MECachedRegionStorage;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(EntityChunkDataAccess.class)
public class MixinEntityChunkDataAccess {

    @Shadow
    @Final
    private StorageIoWorker dataLoadWorker;

    @Shadow
    @Final
    private Executor executor;

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/storage/StorageIoWorker"))
    private StorageIoWorker onStorageIoInit(File file, boolean bl, String string) {
        return new C2MECachedRegionStorage(file, bl, string);
    }

    /**
     * @author ishland
     * @reason await tasks while waiting
     */
    @Overwrite
    public void awaitAll() {
        final CompletableFuture<Void> future = this.dataLoadWorker.completeAll();
        if (this.executor instanceof ThreadExecutor) {
            if (((ThreadExecutor<?>) this.executor).isOnThread()) {
                ((ThreadExecutor<?>) this.executor).runTasks(future::isDone);
            }
        }
        future.join();
    }

}
