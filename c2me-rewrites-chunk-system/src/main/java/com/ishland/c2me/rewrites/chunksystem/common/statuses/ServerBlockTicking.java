package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.google.common.base.Suppliers;
import com.ishland.c2me.base.common.threadstate.ThreadInstrumentation;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.Config;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkHolderVanillaInterface;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.c2me.rewrites.chunksystem.common.ducks.WorldChunkExtension;
import com.ishland.c2me.rewrites.chunksystem.common.quirks.FlowableFluidUtils;
import com.ishland.c2me.rewrites.chunksystem.common.threadstate.ChunkTaskWork;
import com.ishland.flowsched.scheduler.Cancellable;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.ChunkGenerationSteps;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public class ServerBlockTicking extends NewChunkStatus {

    private static final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] deps;

    static {
        deps = new KeyStatusPair[] {
                new KeyStatusPair<>(new ChunkPos(-1, -1), NewChunkStatus.SERVER_ACCESSIBLE),
                new KeyStatusPair<>(new ChunkPos(-1, 0), NewChunkStatus.SERVER_ACCESSIBLE),
                new KeyStatusPair<>(new ChunkPos(-1, 1), NewChunkStatus.SERVER_ACCESSIBLE),
                new KeyStatusPair<>(new ChunkPos(0, -1), NewChunkStatus.SERVER_ACCESSIBLE),
                new KeyStatusPair<>(new ChunkPos(0, 1), NewChunkStatus.SERVER_ACCESSIBLE),
                new KeyStatusPair<>(new ChunkPos(1, -1), NewChunkStatus.SERVER_ACCESSIBLE),
                new KeyStatusPair<>(new ChunkPos(1, 0), NewChunkStatus.SERVER_ACCESSIBLE),
                new KeyStatusPair<>(new ChunkPos(1, 1), NewChunkStatus.SERVER_ACCESSIBLE),
        };
    }

    public ServerBlockTicking(int ordinal) {
        super(ordinal, ChunkStatus.FULL);
    }

    @Override
    public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context, Cancellable cancellable) {
        if (Config.filterFluidPostProcessing) {
            try {
                filterFluidTicks(context);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return CompletableFuture.runAsync(() -> {
            try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, true))) {
                final WorldChunk chunk = (WorldChunk) context.holder().getItem().get().chunk();
                chunk.runPostProcessing(((IThreadedAnvilChunkStorage) context.tacs()).getWorld());
                ((IThreadedAnvilChunkStorage) context.tacs()).getWorld().disableTickSchedulers(chunk);
                sendChunkToPlayer(context);
                ((IThreadedAnvilChunkStorage) context.tacs()).getTotalChunksLoadedCount().incrementAndGet(); // never decremented in vanilla
                ((WorldChunkExtension) chunk).c2me$setBlockTicking(true);
            }
        }, ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor());
    }

    private static void filterFluidTicks(ChunkLoadingContext context) {
        final WorldChunk chunk = (WorldChunk) context.holder().getItem().get().chunk();

        Supplier<ChunkRegion> chunkRegionSupplier = Suppliers.memoize(() -> new ChunkRegion(((IThreadedAnvilChunkStorage) context.tacs()).getWorld(), context.chunks(), ChunkGenerationSteps.GENERATION.get(ChunkStatus.LIGHT), chunk));

        int total = 0;
        int eliminated = 0;
        ShortList[] postProcessingLists = chunk.getPostProcessingLists();
        for (int i = 0; i < postProcessingLists.length; i++) {
            if (postProcessingLists[i] != null) {
                for (ShortListIterator iterator = postProcessingLists[i].iterator(); iterator.hasNext(); ) {
                    Short short_ = iterator.next();
                    BlockPos blockPos = ProtoChunk.joinBlockPos(short_, chunk.sectionIndexToCoord(i), chunk.getPos());
                    BlockState blockState = chunk.getBlockState(blockPos);
                    FluidState fluidState = blockState.getFluidState();
                    if (!fluidState.isEmpty() && fluidState.getFluid() instanceof FlowableFluid) {
                        total ++;
                        if (!FlowableFluidUtils.needsPostProcessing(chunkRegionSupplier.get(), blockPos, blockState, fluidState)) {
//                            iterator.remove();
                            iterator.set((short) (short_ | (0x4000))); // set a flag
                            eliminated ++;
                        }
                    }
                }
            }
        }

//        if (total > 0) {
//            System.out.println(String.format("Eliminated %d/%d (%.2f%%) post processing fluids in chunk %s", eliminated, total, eliminated / (double) total * 100.0, context.holder().getKey()));
//        }
    }

    private static void sendChunkToPlayer(ChunkLoadingContext context) {
        final WorldChunk chunk = (WorldChunk) context.holder().getItem().get().chunk();
        NewChunkHolderVanillaInterface holderVanillaInterface = context.holder().getUserData().get();
        CompletableFuture<?> completableFuturexx = holderVanillaInterface.getPostProcessingFuture();
        if (completableFuturexx.isDone()) {
            ((IThreadedAnvilChunkStorage) context.tacs()).invokeSendToPlayers(holderVanillaInterface, chunk);
        } else {
            completableFuturexx.thenAcceptAsync(v -> ((IThreadedAnvilChunkStorage) context.tacs()).invokeSendToPlayers(holderVanillaInterface, chunk), ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor());
        }
    }

    @Override
    public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context, Cancellable cancellable) {
        final WorldChunk chunk = (WorldChunk) context.holder().getItem().get().chunk();
        ((WorldChunkExtension) chunk).c2me$setBlockTicking(false);
        return CompletableFuture.completedStage(null);
        // TODO check if syncing is needed
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependencies(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return relativeToAbsoluteDependencies(holder, deps);
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToRemove(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return EMPTY_DEPENDENCIES;
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToAdd(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return EMPTY_DEPENDENCIES;
    }

    @Override
    public String toString() {
        return "Block Ticking";
    }
}
