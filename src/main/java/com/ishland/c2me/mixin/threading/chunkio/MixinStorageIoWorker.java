package com.ishland.c2me.mixin.threading.chunkio;

import com.ishland.c2me.common.threading.chunkio.IAsyncChunkStorage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageIoWorker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

@Mixin(StorageIoWorker.class)
public abstract class MixinStorageIoWorker implements IAsyncChunkStorage {

    @Shadow protected abstract CompletableFuture<NbtCompound> readChunkData(ChunkPos pos);

    @Override
    public CompletableFuture<NbtCompound> getNbtAtAsync(ChunkPos pos) {
        return readChunkData(pos);
    }
}
