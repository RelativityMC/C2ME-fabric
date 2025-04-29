package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.google.common.base.Preconditions;
import com.ishland.c2me.base.common.config.ModStatuses;
import com.ishland.c2me.base.common.threadstate.ThreadInstrumentation;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.base.mixin.access.IWorldChunk;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.c2me.rewrites.chunksystem.common.compat.lithium.LithiumChunkStatusTrackerInvoker;
import com.ishland.c2me.rewrites.chunksystem.common.ducks.WorldChunkExtension;
import com.ishland.c2me.rewrites.chunksystem.common.fapi.LifecycleEventInvoker;
import com.ishland.c2me.rewrites.chunksystem.common.threadstate.ChunkTaskWork;
import com.ishland.flowsched.scheduler.Cancellable;
import net.minecraft.class_11352;
import net.minecraft.class_11368;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ErrorReporter;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ServerAccessible extends NewChunkStatus {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerAccessible.class);

    public ServerAccessible(int ordinal) {
        super(ordinal, ChunkStatus.FULL);
    }

    @Override
    public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context, Cancellable cancellable) {
        final Chunk chunk = context.holder().getItem().get().chunk();
        Preconditions.checkState(chunk instanceof ProtoChunk, "Chunk must be a proto chunk");
        ProtoChunk protoChunk = (ProtoChunk) chunk;

        return CompletableFuture.runAsync(() -> {
            try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, true))) {
                ServerWorld serverWorld = ((IThreadedAnvilChunkStorage) context.tacs()).getWorld();
                final WorldChunk worldChunk = toFullChunk(protoChunk, serverWorld);

                worldChunk.setLevelTypeProvider(context.holder().getUserData().get()::getLevelType);
                worldChunk.setUnsavedListener(((IThreadedAnvilChunkStorage) context.tacs()).getGenerationContext().unsavedListener());
                ((WorldChunkExtension) worldChunk).c2me$setBlockTicking(false); // not necessary, but just in case
                context.holder().getItem().set(new ChunkState(worldChunk, new WrapperProtoChunk(worldChunk, false), ChunkStatus.FULL));
                if (!((IWorldChunk) worldChunk).isLoadedToWorld()) {
                    worldChunk.loadEntities();
                    worldChunk.setLoadedToWorld(true);
                    worldChunk.updateAllBlockEntities();
                    worldChunk.addChunkTickSchedulers(serverWorld);
                    if (ModStatuses.fabric_lifecycle_events_v1) {
                        LifecycleEventInvoker.invokeChunkLoaded(serverWorld, worldChunk, !(protoChunk instanceof WrapperProtoChunk));
                    }
                }

                ((IThreadedAnvilChunkStorage) context.tacs()).getCurrentChunkHolders().put(context.holder().getKey().toLong(), context.holder().getUserData().get());
                ((IThreadedAnvilChunkStorage) context.tacs()).setChunkHolderListDirty(true);
            }
        }, ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor());
    }

    private static WorldChunk toFullChunk(ProtoChunk protoChunk, ServerWorld serverWorld) {
        WorldChunk worldChunk;
        if (protoChunk instanceof WrapperProtoChunk) {
            worldChunk = ((WrapperProtoChunk) protoChunk).getWrappedChunk();
        } else {
            worldChunk = new WorldChunk(serverWorld, protoChunk, worldChunkx -> {
                try (ErrorReporter.class_11340 lv = new ErrorReporter.class_11340(protoChunk.method_71412(), LOGGER)) {
                    class_11368.class_11370 arg = class_11352.method_71416(lv, serverWorld.getRegistryManager(), protoChunk.getEntities());
                    if (!arg.method_71444()) {
                        serverWorld.addEntities(EntityType.streamFromNbt(arg, serverWorld, SpawnReason.LOAD));
                    }
                }
            });
        }
        return worldChunk;
    }

    @Override
    public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context, Cancellable cancellable) {
        ChunkState state = context.holder().getItem().get();
        final Chunk chunk = state.chunk();
        Preconditions.checkState(chunk instanceof WorldChunk, "Chunk must be a full chunk");
        return CompletableFuture.runAsync(() -> {
            ((IThreadedAnvilChunkStorage) context.tacs()).getCurrentChunkHolders().remove(context.holder().getKey().toLong());
            ((IThreadedAnvilChunkStorage) context.tacs()).setChunkHolderListDirty(true);
            final WorldChunk worldChunk = (WorldChunk) chunk;
//            worldChunk.setLoadedToWorld(false);
//            worldChunk.removeChunkTickSchedulers(((IThreadedAnvilChunkStorage) context.tacs()).getWorld());
            worldChunk.setLevelTypeProvider(null);
            worldChunk.setUnsavedListener(pos -> {
            });
            LithiumChunkStatusTrackerInvoker.invokeOnChunkInaccessible(((IThreadedAnvilChunkStorage) context.tacs()).getWorld(), context.holder().getKey());
            context.holder().getItem().set(new ChunkState(state.protoChunk(), state.protoChunk(), ChunkStatus.FULL));
        }, ((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor());
    }

    @Override
    public String toString() {
        return "minecraft:full, Border";
    }
}
