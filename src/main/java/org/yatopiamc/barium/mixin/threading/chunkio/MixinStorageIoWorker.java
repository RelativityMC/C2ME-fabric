package org.yatopiamc.barium.mixin.threading.chunkio;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.StorageIoWorker;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.yatopiamc.barium.common.threading.chunkio.ThreadedStorageIo;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Mixin(StorageIoWorker.class)
public abstract class MixinStorageIoWorker implements ThreadedStorageIo {

    @Shadow
    protected abstract <T> CompletableFuture<T> run(Supplier<Either<T, Exception>> supplier);

    @Mutable
    @Shadow
    @Final
    private Map<ChunkPos, StorageIoWorker.Result> results;

    @Shadow
    @Final
    private RegionBasedStorage storage;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.results = new ConcurrentHashMap<>();
    }

    /**
     * @author ishland
     * @reason Write directly
     */
    @Overwrite
    public CompletableFuture<Void> setResult(ChunkPos pos, CompoundTag nbt) {
        StorageIoWorker.Result result = this.results.computeIfAbsent(pos, (chunkPos) -> new StorageIoWorker.Result(nbt));
        result.nbt = nbt;
        return CompletableFuture.completedFuture(null);
    }

    @Inject(method = "getNbt", at = @At("HEAD"), cancellable = true)
    private void onGetNbt(ChunkPos pos, CallbackInfoReturnable<CompoundTag> cir) {
        final StorageIoWorker.Result result1 = this.results.get(pos);
        if (result1 != null) cir.setReturnValue(result1.nbt);
    }

    @Override
    public CompletableFuture<CompoundTag> getNbtAtAsync(ChunkPos pos) {
        final StorageIoWorker.Result result1 = this.results.get(pos);
        if (result1 != null) return CompletableFuture.completedFuture(result1.nbt);
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
