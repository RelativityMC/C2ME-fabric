package com.ishland.c2me.rewrites.chunksystem.common;

import com.ishland.flowsched.scheduler.ItemHolder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class NewChunkHolderVanillaInterface extends ChunkHolder {

    private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.createOrderedList();

    private final ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> newHolder;

    public NewChunkHolderVanillaInterface(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> newHolder, HeightLimitView world, LightingProvider lightingProvider, PlayersWatchingChunkProvider playersWatchingChunkProvider) {
        super(newHolder.getKey(), ChunkLevels.INACCESSIBLE, world, lightingProvider, (pos1, levelGetter, targetLevel, levelSetter) -> {}, playersWatchingChunkProvider);
        this.newHolder = newHolder;
    }

    private CompletableFuture<OptionalChunk<Chunk>> wrapOptionalChunkFuture(CompletableFuture<?> future) {
        return future.thenApply(unused -> OptionalChunk.of(this.newHolder.getItem().get().chunk()));
    }

    private CompletableFuture<OptionalChunk<WorldChunk>> wrapOptionalWorldChunkFuture(CompletableFuture<?> future) {
        return future.thenApply(unused -> OptionalChunk.of((WorldChunk) this.newHolder.getItem().get().chunk()));
    }

    private CompletableFuture<Chunk> wrapChunkFuture(CompletableFuture<?> future) {
        return future.thenApply(unused -> this.newHolder.getItem().get().chunk());
    }

    @Override
    public CompletableFuture<OptionalChunk<Chunk>> getFutureFor(ChunkStatus leastStatus) {
        return wrapOptionalChunkFuture(this.newHolder.getFutureForStatus(NewChunkStatus.fromVanillaStatus(leastStatus)));
    }

    @Override
    public CompletableFuture<OptionalChunk<Chunk>> getValidFutureFor(ChunkStatus leastStatus) {
        return this.getFutureFor(leastStatus); // TODO
    }

    @Override
    public CompletableFuture<OptionalChunk<WorldChunk>> getTickingFuture() {
        return wrapOptionalWorldChunkFuture(this.newHolder.getFutureForStatus(NewChunkStatus.BLOCK_TICKING));
    }

    @Override
    public CompletableFuture<OptionalChunk<WorldChunk>> getEntityTickingFuture() {
        return wrapOptionalWorldChunkFuture(this.newHolder.getFutureForStatus(NewChunkStatus.ENTITY_TICKING));
    }

    @Override
    public CompletableFuture<OptionalChunk<WorldChunk>> getAccessibleFuture() {
        return wrapOptionalWorldChunkFuture(this.newHolder.getFutureForStatus(NewChunkStatus.SERVER_ACCESSIBLE));
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

    @Nullable
    @Override
    public ChunkStatus getCurrentStatus() {
        return ((NewChunkStatus) this.newHolder.getStatus()).getEffectiveVanillaStatus();
    }

    @Nullable
    @Override
    public Chunk getCurrentChunk() {
        return this.newHolder.getItem().get().chunk();
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
    public CompletableFuture<OptionalChunk<Chunk>> getChunkAt(ChunkStatus targetStatus, ThreadedAnvilChunkStorage chunkStorage) {
        return this.getFutureFor(targetStatus); // TODO ensure future is present
    }

    @Override
    protected void combineSavingFuture(String thenDesc, CompletableFuture<?> then) {
        this.newHolder.submitOp(then.handle((o, throwable) -> null));
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
        return ChunkLevels.getLevelFromStatus(((NewChunkStatus) this.newHolder.getTargetStatus()).getEffectiveVanillaStatus());
    }

    @Override
    public int getCompletedLevel() {
        return ChunkLevels.getLevelFromStatus(((NewChunkStatus) this.newHolder.getStatus()).getEffectiveVanillaStatus());
    }

    @Override
    public void setLevel(int level) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void updateFutures(ThreadedAnvilChunkStorage chunkStorage, Executor executor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAccessible() {
        return this.newHolder.getStatus().ordinal() >= NewChunkStatus.SERVER_ACCESSIBLE.ordinal();
    }

    @Override
    public void updateAccessibleStatus() {
        // no-op
    }

    @Override
    public void setCompletedChunk(WrapperProtoChunk chunk) {
        // no-op
    }

    @Override
    public List<Pair<ChunkStatus, CompletableFuture<OptionalChunk<Chunk>>>> collectFuturesByStatus() {
        List<Pair<ChunkStatus, CompletableFuture<OptionalChunk<Chunk>>>> list = new ArrayList<>();

        for (final ChunkStatus status : CHUNK_STATUSES) {
            list.add(Pair.of(status, this.getFutureFor(status)));
        }

        return list;
    }
}
