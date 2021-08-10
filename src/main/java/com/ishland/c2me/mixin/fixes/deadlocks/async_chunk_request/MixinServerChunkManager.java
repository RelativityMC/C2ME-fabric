package com.ishland.c2me.mixin.fixes.deadlocks.async_chunk_request;

import com.ishland.c2me.common.util.CFUtil;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
    public abstract boolean tick();

    @Shadow @Final public ThreadedAnvilChunkStorage threadedAnvilChunkStorage;
    @Shadow @Final public ServerChunkManager.MainThreadExecutor mainThreadExecutor;
    private static final ChunkTicketType<ChunkPos> ASYNC_LOAD = ChunkTicketType.create("async_load", Comparator.comparingLong(ChunkPos::toLong));

    @Inject(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At("HEAD"), cancellable = true)
    private void onGetChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir) {
        if (Thread.currentThread() != this.serverThread) {
            cir.setReturnValue(CFUtil.join(CompletableFuture.supplyAsync(() -> {
                // TODO [VanillaCopy] getChunkFuture
                ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                long chunkPosLong = chunkPos.toLong();
                int ticketLevel = 33 + ChunkStatus.getDistanceFromFull(leastStatus);
                ChunkHolder chunkHolder = this.getChunkHolder(chunkPosLong);
                if (create) {
                    this.ticketManager.addTicketWithLevel(ASYNC_LOAD, chunkPos, ticketLevel, chunkPos);
                    if (this.isMissingForLevel(chunkHolder, ticketLevel)) {
                        Profiler profiler = this.world.getProfiler();
                        profiler.push("chunkLoad");
                        this.tick();
                        chunkHolder = this.getChunkHolder(chunkPosLong);
                        profiler.pop();
                        if (this.isMissingForLevel(chunkHolder, ticketLevel)) {
                            throw Util.throwOrPause(new IllegalStateException("No chunk holder after ticket has been added"));
                        }
                    }
                }

                final CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> future = this.isMissingForLevel(chunkHolder, ticketLevel) ? ChunkHolder.UNLOADED_CHUNK_FUTURE : chunkHolder.getChunkAt(leastStatus, this.threadedAnvilChunkStorage);
                if (create) {
                    future.exceptionally(__ -> null).thenRunAsync(() -> {
                        this.ticketManager.removeTicketWithLevel(ASYNC_LOAD, chunkPos, ticketLevel, chunkPos);
                    }, this.mainThreadExecutor);
                }
                return future;
            }, this.mainThreadExecutor).thenCompose(Function.identity()).thenApply(either -> either.map(Function.identity(), unloaded -> {
                if (create) {
                    throw Util.throwOrPause(new IllegalStateException("Chunk not there when requested: " + unloaded));
                } else {
                    return null;
                }
            }))));
        }
    }

}
