package com.ishland.c2me.rewrites.chunk_serializer.mixin;

import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
@Mixin(VersionedChunkStorage.class)
public interface VersionedChunkStorageAccessor {
    @Accessor("worker")
    StorageIoWorker getIoWorker();
}
