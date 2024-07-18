package com.ishland.c2me.rewrites.chunksystem.mixin.async_serialization;

import com.mojang.datafixers.DataFixer;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageKey;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkLoadingManager.class)
public abstract class MixinThreadedAnvilChunkStorage extends VersionedChunkStorage implements ChunkHolder.PlayersWatchingChunkProvider {

    public MixinThreadedAnvilChunkStorage(StorageKey arg, Path path, DataFixer dataFixer, boolean bl) {
        super(arg, path, dataFixer, bl);
    }

    @Shadow protected abstract NbtCompound updateChunkNbt(NbtCompound nbt);

    /**
     * @author ishland
     * @reason skip datafixer if possible
     */
    @Overwrite
    private CompletableFuture<Optional<NbtCompound>> getUpdatedChunkNbt(ChunkPos chunkPos) {
//        return this.getNbt(chunkPos).thenApplyAsync(nbt -> nbt.map(this::updateChunkNbt), Util.getMainWorkerExecutor());
        return this.getNbt(chunkPos).thenCompose(nbt -> {
            if (nbt.isPresent()) {
                final NbtCompound compound = nbt.get();
                if (VersionedChunkStorage.getDataVersion(compound) != SharedConstants.getGameVersion().getSaveVersion().getId()) {
                    return CompletableFuture.supplyAsync(() -> Optional.of(updateChunkNbt(compound)), Util.getMainWorkerExecutor());
                } else {
                    return CompletableFuture.completedFuture(nbt);
                }
            } else {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        });
    }

}
