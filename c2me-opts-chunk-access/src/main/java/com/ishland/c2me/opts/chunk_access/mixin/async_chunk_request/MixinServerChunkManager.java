package com.ishland.c2me.opts.chunk_access.mixin.async_chunk_request;

import com.ishland.c2me.base.common.util.CFUtil;
import com.ishland.c2me.opts.chunk_access.common.CurrentWorldGenState;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WrapperProtoChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(ServerChunkManager.class)
public abstract class MixinServerChunkManager {

    @Shadow
    @Final
    private Thread serverThread;

    @Shadow
    @Nullable
    protected abstract ChunkHolder getChunkHolder(long pos);

    @Shadow
    @Final
    private ChunkTicketManager ticketManager;

    @Shadow
    protected abstract boolean isMissingForLevel(@Nullable ChunkHolder holder, int maxLevel);

    @Shadow
    @Final
    ServerWorld world;

    @Shadow
    public abstract boolean updateChunks();

    @Shadow @Final public ServerChunkManager.MainThreadExecutor mainThreadExecutor;
    @Shadow @Final public ServerChunkLoadingManager chunkLoadingManager;

    @Shadow
    protected abstract CompletableFuture<OptionalChunk<Chunk>> getChunkFuture(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create);

    private static final ChunkTicketType<ChunkPos> ASYNC_LOAD = ChunkTicketType.create("async_load", Comparator.comparingLong(ChunkPos::toLong));

    @Inject(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At("HEAD"), cancellable = true)
    private void onGetChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir) {
        if (Thread.currentThread() != this.serverThread) {
            cir.setReturnValue(c2me$getChunkOffThread(chunkX, chunkZ, leastStatus, create));
        }
    }

    @Unique
    @Final
    private Chunk c2me$getChunkOffThread(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        // shortcut for worldgen threads
        final ChunkRegion currentRegion = CurrentWorldGenState.getCurrentRegion();
        if (currentRegion != null) {
            Chunk chunk = currentRegion.getChunk(chunkX, chunkZ, leastStatus, false);
            if (chunk instanceof WrapperProtoChunk readOnlyChunk) chunk = readOnlyChunk.getWrappedChunk();
            if (chunk != null) return chunk;
        }

        // shortcut if already exists
        final ChunkHolder available = this.getChunkHolder(ChunkPos.toLong(chunkX, chunkZ)); // thread-safe
        if (available != null) {
            final Chunk availableChunk = available.getOrNull(leastStatus);
            if (availableChunk != null) {
                return availableChunk;
            }
        }

        final CompletableFuture<Chunk> chunkLoad = c2me$getChunkFutureOffThread(chunkX, chunkZ, leastStatus, create);
        assert chunkLoad != null;
        return CFUtil.join(chunkLoad);
    }

    @Unique
    @Final
    @Nullable
    private CompletableFuture<Chunk> c2me$getChunkFutureOffThread(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        return CompletableFuture.supplyAsync(() -> this.getChunkFuture(chunkX, chunkZ, leastStatus, create), this.mainThreadExecutor)
                .thenCompose(Function.identity()).thenApply(either -> {
            final Chunk chunk = either.orElse(null);
            if (chunk == null && create) {
                throw new IllegalStateException("Chunk not there when requested: " + either.getError());
            }
            return chunk;
        });
    }

}
