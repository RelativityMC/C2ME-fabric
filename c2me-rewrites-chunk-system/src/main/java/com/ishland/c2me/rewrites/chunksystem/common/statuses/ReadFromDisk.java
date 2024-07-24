package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.ishland.c2me.base.mixin.access.IServerLightingProvider;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.base.mixin.access.IVersionedChunkStorage;
import com.ishland.c2me.base.mixin.access.IWorldChunk;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ReadFromDisk extends NewChunkStatus {

    private static final Logger LOGGER = LoggerFactory.getLogger("ReadFromDisk");

    public ReadFromDisk(int ordinal) {
        super(ordinal, ChunkStatus.EMPTY);
    }

    @Override
    public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
        return ((IThreadedAnvilChunkStorage) context.tacs())
                .invokeGetUpdatedChunkNbt(context.holder().getKey())
                .thenApply(nbt -> nbt.filter(nbt2 -> {
                    boolean bl = nbt2.contains("Status", NbtElement.STRING_TYPE);
                    if (!bl) {
                        LOGGER.error("Chunk file at {} is missing level data, skipping", context.holder().getKey());
                    }

                    return bl;
                }))
                .thenApplyAsync(nbt -> {
                    if (nbt.isPresent()) {
                        Chunk chunk = ChunkSerializer.deserialize(
                                ((IThreadedAnvilChunkStorage) context.tacs()).getWorld(),
                                ((IThreadedAnvilChunkStorage) context.tacs()).getPointOfInterestStorage(),
                                ((IVersionedChunkStorage) context.tacs()).invokeGetStorageKey(),
                                context.holder().getKey(),
                                nbt.get()
                        );
                        return chunk;
                    } else {
                        return null;
                    }
                }, ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor())
//                .exceptionallyAsync(throwable -> this.recoverFromException(throwable, pos), ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor())
                .thenApply(chunk -> {
                    if (chunk == null) {
                        final ServerWorld world = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
                        return new ProtoChunk(context.holder().getKey(), UpgradeData.NO_UPGRADE_DATA, world, world.getRegistryManager().get(RegistryKeys.BIOME), null);
                    } else {
                        return chunk;
                    }
                })
                .thenAccept(chunk -> context.holder().getItem().set(new ChunkState(chunk)));
    }

    @Override
    public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
        return CompletableFuture.runAsync(() -> {
            Chunk chunk = context.holder().getItem().get().chunk();
            if (chunk instanceof WrapperProtoChunk protoChunk) chunk = protoChunk.getWrappedChunk();

            final boolean loadedToWorld;
            if (chunk instanceof WorldChunk worldChunk) {
                loadedToWorld = ((IWorldChunk) worldChunk).isLoadedToWorld();
                worldChunk.setLoadedToWorld(false);
            } else {
                loadedToWorld = false;
            }

            if ((context.holder().getFlags() & ItemHolder.FLAG_BROKEN) == 0 || chunk instanceof WorldChunk) { // do not save broken ProtoChunks
                ((IThreadedAnvilChunkStorage) context.tacs()).invokeSave(chunk);
            } else {
                LOGGER.warn("Not saving partially generated broken chunk {}", context.holder().getKey());
            }
            if (loadedToWorld && chunk instanceof WorldChunk worldChunk) {
                ((IThreadedAnvilChunkStorage) context.tacs()).getWorld().unloadEntities(worldChunk);
            }

            ((IServerLightingProvider) ((IThreadedAnvilChunkStorage) context.tacs()).getLightingProvider()).invokeUpdateChunkStatus(chunk.getPos());
            ((IThreadedAnvilChunkStorage) context.tacs()).getLightingProvider().tick();
            ((IThreadedAnvilChunkStorage) context.tacs()).getWorldGenerationProgressListener().setChunkStatus(chunk.getPos(), null);
            ((IThreadedAnvilChunkStorage) context.tacs()).getChunkToNextSaveTimeMs().remove(chunk.getPos().toLong());

            context.holder().getItem().set(new ChunkState(null));
        }, ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor());
    }

    @Override
    public String toString() {
        return "minecraft:empty";
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToRemove(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return EMPTY_DEPENDENCIES;
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToAdd(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return EMPTY_DEPENDENCIES;
    }
}
