/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.notickvd.mixin;

import com.google.common.collect.ImmutableList;
import com.ishland.c2me.base.common.threadstate.ThreadInstrumentation;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.Config;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkHolderVanillaInterface;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.c2me.rewrites.chunksystem.common.statuses.ServerAccessibleChunkSending;
import com.ishland.c2me.rewrites.chunksystem.common.threadstate.ChunkTaskWork;
import com.ishland.flowsched.scheduler.Cancellable;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import com.ishland.flowsched.util.Assertions;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationSteps;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Mixin(ServerAccessibleChunkSending.class)
public class MixinServerAccessibleChunkSending {

    @Mutable
    @Shadow(remap = false)
    @Final
    private static KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] deps;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onCLInit(CallbackInfo ci) {
        NewChunkStatus depStatus = NewChunkStatus.fromVanillaStatus(ChunkStatus.LIGHT);
        deps = new KeyStatusPair[]{
                new KeyStatusPair<>(new ChunkPos(-1, -1), depStatus),
                new KeyStatusPair<>(new ChunkPos(-1, 0), depStatus),
                new KeyStatusPair<>(new ChunkPos(-1, 1), depStatus),
                new KeyStatusPair<>(new ChunkPos(0, -1), depStatus),
                new KeyStatusPair<>(new ChunkPos(0, 1), depStatus),
                new KeyStatusPair<>(new ChunkPos(1, -1), depStatus),
                new KeyStatusPair<>(new ChunkPos(1, 0), depStatus),
                new KeyStatusPair<>(new ChunkPos(1, 1), depStatus),
        };
    }

    /**
     * @author ishland
     * @reason do chunk sending
     */
    @Overwrite(remap = false)
    public Completable upgradeToThis(ChunkLoadingContext context, Cancellable cancellable) {
        ArrayList<BlockPos> blocksToRemove = new ArrayList<>();
        if (Config.suppressGhostMushrooms) {
            ServerWorld serverWorld = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
            ChunkState state = context.holder().getItem().get();
            ChunkPos chunkPos = context.holder().getKey();

            BoundedRegionArray<AbstractChunkHolder> boundedRegionArray = BoundedRegionArray.create(chunkPos.x(), chunkPos.z(), 1, (x, z) -> context.theChunkSystem().getHolder(new ChunkPos(x, z)).getUserData().get());
            ChunkRegion chunkRegion = new ChunkRegion(serverWorld, boundedRegionArray, ChunkGenerationSteps.GENERATION.get(ChunkStatus.FULL), state.protoChunk());
            Chunk chunk = state.chunk();

            ShortList[] postProcessingLists = chunk.getPostProcessingLists();
            for (int i = 0; i < postProcessingLists.length; i++) {
                if (postProcessingLists[i] != null) {
                    for (ShortListIterator iterator = postProcessingLists[i].iterator(); iterator.hasNext(); ) {
                        short short_ = iterator.nextShort();
                        BlockPos blockPos = ProtoChunk.joinBlockPos(short_, chunk.sectionIndexToCoord(i), chunkPos);
                        BlockState blockState = chunk.getBlockState(blockPos);

                        if (blockState.getBlock() == Blocks.BROWN_MUSHROOM || blockState.getBlock() == Blocks.RED_MUSHROOM) {
                            if (!blockState.canPlaceAt(chunkRegion, blockPos)) {
                                blocksToRemove.add(blockPos);
                            }
                        }
                    }
                }
            }
        }
        return Completable
                .fromRunnable(() -> {
                    Assertions.assertTrue(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor().isOnThread());

                    try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, (ServerAccessibleChunkSending) (Object) this, true))) {
                        if (Config.suppressGhostMushrooms) {
                            ServerWorld serverWorld = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
                            ChunkState state = context.holder().getItem().get();
                            Chunk chunk = state.chunk();
                            for (BlockPos blockPos : blocksToRemove) {
                                serverWorld.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NO_REDRAW | Block.FORCE_STATE);
                            }
                            for (BlockPos blockPos2 : ImmutableList.copyOf(chunk.getBlockEntityPositions())) {
                                chunk.getBlockEntity(blockPos2);
                            }
                        }
                        sendChunkToPlayer(context.tacs(), context.holder());
                    }
                })
                .subscribeOn(Schedulers.from(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()));
    }

    @Unique
    private static void sendChunkToPlayer(ServerChunkLoadingManager tacs, ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder) {
        final WorldChunk worldChunk = (WorldChunk) holder.getItem().get().chunk();
        NewChunkHolderVanillaInterface holderVanillaInterface = holder.getUserData().get();
        CompletableFuture<?> completableFuturexx = holderVanillaInterface.getPostProcessingFuture();
        if (completableFuturexx.isDone()) {
            ((IThreadedAnvilChunkStorage) tacs).invokeSendToPlayers(holderVanillaInterface, worldChunk);
        } else {
            completableFuturexx.thenAcceptAsync(v -> ((IThreadedAnvilChunkStorage) tacs).invokeSendToPlayers(holderVanillaInterface, worldChunk), ((IThreadedAnvilChunkStorage) tacs).getMainThreadExecutor());
        }
    }

}
