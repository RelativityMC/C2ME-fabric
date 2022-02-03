package com.ishland.c2me.mixin.optimization.chunkscheduling.idle_tasks;

import com.ishland.c2me.common.optimization.chunkscheduling.idle_tasks.IThreadedAnvilChunkStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage implements IThreadedAnvilChunkStorage {

    @Shadow @Final private Long2ObjectLinkedOpenHashMap<ChunkHolder> currentChunkHolders;

    @Shadow protected abstract boolean save(ChunkHolder chunkHolder);

    @Unique
    private static final Long2ObjectLinkedOpenHashMap<ChunkHolder> anEmptyChunkHoldersMap = new Long2ObjectLinkedOpenHashMap<>();

    @Unique
    private final Object2LongLinkedOpenHashMap<ChunkPos> dirtyChunkPosForAutoSave = new Object2LongLinkedOpenHashMap<>();

    @Redirect(method = "unloadChunks", at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;chunkHolders:Lit/unimi/dsi/fastutil/longs/Long2ObjectLinkedOpenHashMap;"))
    private Long2ObjectLinkedOpenHashMap<ChunkHolder> stopAutoSaveInUnloading(ThreadedAnvilChunkStorage instance) {
        return anEmptyChunkHoldersMap; // prevent autosave from happening in unloading stage
    }

    @Override
    public void enqueueDirtyChunkPosForAutoSave(ChunkPos chunkPos) {
        synchronized (this.dirtyChunkPosForAutoSave) {
            this.dirtyChunkPosForAutoSave.putAndMoveToLast(chunkPos, System.currentTimeMillis());
        }
    }

    @Override
    public boolean runOneChunkAutoSave() {
        synchronized (this.dirtyChunkPosForAutoSave) {
            final ObjectBidirectionalIterator<Object2LongMap.Entry<ChunkPos>> iterator = this.dirtyChunkPosForAutoSave.object2LongEntrySet().fastIterator();
            while (iterator.hasNext()) {
                final Object2LongMap.Entry<ChunkPos> entry = iterator.next();
                if (System.currentTimeMillis() - entry.getLongValue() < 10000L) break;
                iterator.remove();
                ChunkHolder chunkHolder = this.currentChunkHolders.get(entry.getKey().toLong());
                if (chunkHolder == null) continue;
                final CompletableFuture<Chunk> savingFuture = chunkHolder.getSavingFuture();
                if (savingFuture.isDone()) {
                    this.save(chunkHolder);
                    return true;
                } else {
                    savingFuture.handle((chunk, throwable) -> {
                        this.enqueueDirtyChunkPosForAutoSave(chunkHolder.getPos());
                        return null;
                    });
                }
            }
        }

        return false;
    }
}
