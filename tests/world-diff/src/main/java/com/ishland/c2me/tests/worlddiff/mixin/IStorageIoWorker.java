package com.ishland.c2me.tests.worlddiff.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageIoWorker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

@Mixin(StorageIoWorker.class)
public interface IStorageIoWorker {

    @Invoker
    CompletableFuture<NbtCompound> invokeReadChunkData(ChunkPos pos);

}
