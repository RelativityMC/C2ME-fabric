package com.ishland.c2me.base.mixin.access;

import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.StorageKey;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VersionedChunkStorage.class)
public interface IVersionedChunkStorage {

    @Accessor
    StorageIoWorker getWorker();

    @Invoker
    StorageKey invokeGetStorageKey();

}
