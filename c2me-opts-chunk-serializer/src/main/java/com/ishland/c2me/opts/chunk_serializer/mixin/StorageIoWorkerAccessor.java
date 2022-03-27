package com.ishland.c2me.opts.chunk_serializer.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.StorageIoWorker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mixin(StorageIoWorker.class)
public interface StorageIoWorkerAccessor {
    @Invoker
    <T> CompletableFuture<T> invokeRun(Supplier<Either<T, Exception>> task);

    @Accessor
    RegionBasedStorage getStorage();
}
