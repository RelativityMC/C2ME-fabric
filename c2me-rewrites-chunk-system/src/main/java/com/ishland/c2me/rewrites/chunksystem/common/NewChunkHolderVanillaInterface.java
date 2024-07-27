package com.ishland.c2me.rewrites.chunksystem.common;

import com.ishland.c2me.base.common.util.SneakyThrow;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkLoadingManager;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkLoader;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public class NewChunkHolderVanillaInterface extends ChunkHolder {

    private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.createOrderedList();

    private final ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> newHolder;

    public NewChunkHolderVanillaInterface(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> newHolder, HeightLimitView world, LightingProvider lightingProvider, PlayersWatchingChunkProvider playersWatchingChunkProvider) {
        super(newHolder.getKey(), ChunkLevels.INACCESSIBLE, world, lightingProvider, (pos1, levelGetter, targetLevel, levelSetter) -> {}, playersWatchingChunkProvider);
        this.newHolder = newHolder;
    }

    private CompletableFuture<OptionalChunk<Chunk>> wrapOptionalChunkFuture(CompletableFuture<?> future) {
        if (future.isCompletedExceptionally() && future.exceptionNow() == ItemHolder.UNLOADED_EXCEPTION) {
            return ChunkHolder.UNLOADED_FUTURE;
        }
        return future.handle((unused, throwable) -> {
            while (throwable instanceof CompletionException) {
                throwable = throwable.getCause();
            }
            if (throwable == ItemHolder.UNLOADED_EXCEPTION) {
                return ChunkHolder.UNLOADED;
            } else if (throwable != null) {
                SneakyThrow.sneaky(throwable);
                return null;
            } else {
                return OptionalChunk.of(this.newHolder.getItem().get().chunk());
            }
        });
    }

    private CompletableFuture<OptionalChunk<Chunk>> wrapOptionalChunkProtoFuture(CompletableFuture<Void> future) {
        return wrapOptionalChunkFuture(future).thenApply(optional -> optional.map(chunk -> {
            if (chunk instanceof WorldChunk worldChunk) {
                return new WrapperProtoChunk(worldChunk, false);
            } else {
                return chunk;
            }
        }));
    }

    private CompletableFuture<OptionalChunk<WorldChunk>> wrapOptionalWorldChunkFuture(CompletableFuture<Void> future) {
        if (future.isCompletedExceptionally() && future.exceptionNow() == ItemHolder.UNLOADED_EXCEPTION) {
            return ChunkHolder.UNLOADED_WORLD_CHUNK_FUTURE;
        }
        return future.handle((unused, throwable) -> {
            while (throwable instanceof CompletionException) {
                throwable = throwable.getCause();
            }
            if (throwable == ItemHolder.UNLOADED_EXCEPTION) {
                return ChunkHolder.UNLOADED_WORLD_CHUNK;
            } else if (throwable != null) {
                SneakyThrow.sneaky(throwable);
                return null;
            } else {
                final Chunk chunk = this.newHolder.getItem().get().chunk();
                if (chunk instanceof WorldChunk worldChunk) {
                    return OptionalChunk.of(worldChunk);
                } else {
                    return ChunkHolder.UNLOADED_WORLD_CHUNK; // might have unloaded at this point
                }
            }
        });
    }

    private CompletableFuture<Chunk> wrapChunkFuture(CompletableFuture<?> future) {
        return future.thenApply(unused -> this.newHolder.getItem().get().chunk());
    }

    @Override
    public CompletableFuture<OptionalChunk<Chunk>> load(ChunkStatus requestedStatus, ServerChunkLoadingManager chunkLoadingManager) {
        final CompletableFuture<Void> futureForStatus = this.newHolder.getFutureForStatus(NewChunkStatus.fromVanillaStatus(requestedStatus));
        return requestedStatus == ChunkStatus.FULL ? wrapOptionalChunkFuture(futureForStatus) : wrapOptionalChunkProtoFuture(futureForStatus);
    }

    @Override
    public CompletableFuture<OptionalChunk<WorldChunk>> getTickingFuture() {
        synchronized (this.newHolder) {
            return wrapOptionalWorldChunkFuture(this.newHolder.getFutureForStatus(NewChunkStatus.BLOCK_TICKING));
        }
    }

    @Override
    public CompletableFuture<OptionalChunk<WorldChunk>> getEntityTickingFuture() {
        synchronized (this.newHolder) {
            return wrapOptionalWorldChunkFuture(this.newHolder.getFutureForStatus(NewChunkStatus.ENTITY_TICKING));
        }
    }

    @Override
    public CompletableFuture<OptionalChunk<WorldChunk>> getAccessibleFuture() {
        synchronized (this.newHolder) {
            return wrapOptionalWorldChunkFuture(this.newHolder.getFutureForStatus(NewChunkStatus.SERVER_ACCESSIBLE));
        }
    }

    @Nullable
    @Override
    public WorldChunk getWorldChunk() {
        return this.getTickingFuture().getNow(UNLOADED_WORLD_CHUNK).orElse(null); // TODO
    }

    @Override
    public CompletableFuture<?> getPostProcessingFuture() {
        return super.getPostProcessingFuture(); // use vanilla impl
    }

    @Nullable
    @Override
    public WorldChunk getPostProcessedChunk() {
        return super.getPostProcessedChunk(); // use vanilla impl
    }

    @Override
    public CompletableFuture<Chunk> getSavingFuture() {
        return wrapChunkFuture(this.newHolder.getOpFuture());
    }

    @Override
    public void markForBlockUpdate(BlockPos pos) {
        super.markForBlockUpdate(pos); // use vanilla impl
    }

    @Override
    public void markForLightUpdate(LightType lightType, int y) {
        super.markForLightUpdate(lightType, y); // use vanilla impl
    }

    @Override
    public void flushUpdates(WorldChunk chunk) {
        super.flushUpdates(chunk); // use vanilla impl
    }

    @Override
    public void combinePostProcessingFuture(CompletableFuture<?> postProcessingFuture) {
        super.combinePostProcessingFuture(postProcessingFuture); // use vanilla impl
    }

    @Override
    public ChunkLevelType getLevelType() {
        return ChunkLevels.getType(this.getLevel());
    }

    @Override
    public ChunkPos getPos() {
        return this.newHolder.getKey();
    }

    @Override
    public int getLevel() {
        return ((NewChunkStatus) this.newHolder.getTargetStatus()).toVanillaLevel();
    }

    @Override
    public int getCompletedLevel() {
        return ((NewChunkStatus) this.newHolder.getStatus()).toVanillaLevel();
    }

    @Override
    public void setLevel(int level) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void updateFutures(ServerChunkLoadingManager chunkStorage, Executor executor) {
        // no-op
    }

    @Override
    public boolean isAccessible() {
        return this.newHolder.getStatus().ordinal() >= NewChunkStatus.SERVER_ACCESSIBLE.ordinal();
    }

    @Override
    public boolean isSavable() {
        return this.getRefCount() == 0 && this.getSavingFuture().isDone();
    }

    @Override
    public void updateAccessibleStatus() {
        // no-op
    }

    @Override
    protected void updateStatus(ServerChunkLoadingManager chunkLoadingManager) {
        // no-op
    }

    @Override
    public void replaceWith(WrapperProtoChunk chunk) {
        // no-op
    }

    @Override
    public List<Pair<ChunkStatus, CompletableFuture<OptionalChunk<Chunk>>>> enumerateFutures() {
        List<Pair<ChunkStatus, CompletableFuture<OptionalChunk<Chunk>>>> list = new ArrayList<>();

        for (final ChunkStatus status : CHUNK_STATUSES) {
            final CompletableFuture<Void> futureForStatus = this.newHolder.getFutureForStatus(NewChunkStatus.fromVanillaStatus(status));
            list.add(Pair.of(status, status == ChunkStatus.FULL ? wrapOptionalChunkFuture(futureForStatus) : wrapOptionalChunkProtoFuture(futureForStatus)));
        }

        return list;
    }

    @Override
    public void incrementRefCount() {
        super.incrementRefCount(); // use vanilla impl
    }

    @Override
    public void decrementRefCount() {
        super.decrementRefCount(); // use vanilla impl
    }

    @Override
    public int getRefCount() {
        return super.getRefCount(); // use vanilla impl
    }

    @Nullable
    @Override
    public Chunk getUncheckedOrNull(ChunkStatus requestedStatus) {
        return this.newHolder.getStatus().ordinal() >= NewChunkStatus.fromVanillaStatus(requestedStatus).ordinal()
                ? this.newHolder.getItem().get().chunk() : null;
    }

    @Nullable
    @Override
    public Chunk getOrNull(ChunkStatus requestedStatus) {
        return this.newHolder.getTargetStatus().ordinal() >= NewChunkStatus.fromVanillaStatus(requestedStatus).ordinal()
                ? this.getUncheckedOrNull(requestedStatus) : null;
    }

    @Nullable
    @Override
    public Chunk getLatest() {
        return this.newHolder.getItem().get().chunk();
    }

    @Nullable
    @Override
    public ChunkStatus getActualStatus() {
        final Chunk chunk = this.getLatest();
        return chunk != null ? chunk.getStatus() : null;
    }

    @Nullable
    @Override
    public ChunkStatus getLatestStatus() {
        return ((NewChunkStatus) this.newHolder.getStatus()).getEffectiveVanillaStatus();
    }

    @Override
    protected void combineSavingFuture(CompletableFuture<?> savingFuture) {
        this.newHolder.submitOp(savingFuture.thenAccept(o -> {}));
    }

    @Override
    protected void setCompletedLevel(int level) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected CompletableFuture<OptionalChunk<Chunk>> generate(ChunkGenerationStep step, ChunkLoadingManager chunkLoadingManager, BoundedRegionArray<AbstractChunkHolder> chunks) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void clearLoader(ChunkLoader loader) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void createLoader(ServerChunkLoadingManager chunkLoadingManager, @Nullable ChunkStatus requestedStatus) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected CompletableFuture<OptionalChunk<Chunk>> getOrCreateFuture(ChunkStatus status) {
        final CompletableFuture<Void> futureForStatus = this.newHolder.getFutureForStatus(NewChunkStatus.fromVanillaStatus(status));
        return status == ChunkStatus.FULL ? this.wrapOptionalChunkFuture(futureForStatus) : this.wrapOptionalChunkProtoFuture(futureForStatus);
    }

    @Override
    protected void unload(@Nullable ChunkStatus from, ChunkStatus to) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void unload(int statusIndex, CompletableFuture<OptionalChunk<Chunk>> previousFuture) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void completeChunkFuture(ChunkStatus status, Chunk chunk) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    protected ChunkStatus getMaxPendingStatus(@Nullable ChunkStatus checkUpperBound) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    protected boolean progressStatus(ChunkStatus nextStatus) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean cannotBeLoaded(ChunkStatus status) {
        ChunkStatus chunkStatus = ((NewChunkStatus) this.newHolder.getTargetStatus()).getEffectiveVanillaStatus();
        return chunkStatus == null || status.isLaterThan(chunkStatus);
    }
}
