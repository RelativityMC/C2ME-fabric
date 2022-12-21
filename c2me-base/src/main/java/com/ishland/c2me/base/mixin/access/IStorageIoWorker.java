package com.ishland.c2me.base.mixin.access;

import net.minecraft.world.storage.StorageIoWorker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.BitSet;
import java.util.concurrent.CompletableFuture;

@Mixin(StorageIoWorker.class)
public interface IStorageIoWorker {

    @Invoker
    CompletableFuture<BitSet> invokeGetOrComputeBlendingStatus(int chunkX, int chunkZ);

}
