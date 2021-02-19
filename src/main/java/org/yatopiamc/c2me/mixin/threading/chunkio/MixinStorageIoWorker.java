package org.yatopiamc.c2me.mixin.threading.chunkio;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.util.thread.TaskQueue;
import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.StorageIoWorker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.threading.chunkio.C2MECachedRegionStorage;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(StorageIoWorker.class)
public abstract class MixinStorageIoWorker {

    @Mutable
    @Shadow @Final private RegionBasedStorage storage;

    @Mutable
    @Shadow @Final private Map<ChunkPos, StorageIoWorker.Result> results;

    @Mutable
    @Shadow @Final private TaskExecutor<TaskQueue.PrioritizedTask> field_24468;

    @Shadow @Final private AtomicBoolean closed;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onPostInit(CallbackInfo info) {
        //noinspection ConstantConditions
        if (((Object) this) instanceof C2MECachedRegionStorage) {
            this.storage = null;
            this.results = null;
            this.field_24468 = null;
            this.closed.set(true);
        }
    }

}
