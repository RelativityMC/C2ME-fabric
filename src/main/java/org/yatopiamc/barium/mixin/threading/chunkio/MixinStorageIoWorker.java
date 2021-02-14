package org.yatopiamc.barium.mixin.threading.chunkio;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.StorageIoWorker;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.yatopiamc.barium.common.threading.chunkio.ThreadedStorageIo;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mixin(StorageIoWorker.class)
public abstract class MixinStorageIoWorker implements ThreadedStorageIo {

    @Shadow
    protected abstract <T> CompletableFuture<T> run(Supplier<Either<T, Exception>> supplier);

    @Shadow
    @Final
    private Map<ChunkPos, StorageIoWorker.Result> results;

    @Shadow
    @Final
    private RegionBasedStorage storage;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Override
    public CompletableFuture<CompoundTag> getNbtAtAsync(ChunkPos pos) {
        // [VanillaCopy]
        return this.run(() -> {
            StorageIoWorker.Result result = this.results.get(pos);
            if (result != null) {
                return Either.left(result.nbt);
            } else {
                try {
                    CompoundTag compoundTag = this.storage.getTagAt(pos);
                    return Either.left(compoundTag);
                } catch (Exception var4) {
                    LOGGER.warn("Failed to read chunk {}", pos, var4);
                    return Either.right(var4);
                }
            }
        });
    }

}
