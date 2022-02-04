package com.ishland.c2me.mixin.optimization.chunkscheduling.idle_tasks.autosave.enhanced_autosave;

import com.ishland.c2me.common.config.C2MEConfig;
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

import java.util.concurrent.CompletableFuture;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage implements IThreadedAnvilChunkStorage {

    @Shadow @Final private Long2ObjectLinkedOpenHashMap<ChunkHolder> currentChunkHolders;

    @Shadow protected abstract boolean save(ChunkHolder chunkHolder);

    @Unique
    private final Object2LongLinkedOpenHashMap<ChunkPos> dirtyChunkPosForAutoSave = new Object2LongLinkedOpenHashMap<>();

    @Override
    public void enqueueDirtyChunkPosForAutoSave(ChunkPos chunkPos) {
        if (chunkPos == null) {
            return;
        }
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
                if (System.currentTimeMillis() - entry.getLongValue() < C2MEConfig.generalOptimizationsConfig.autoSaveConfig.delay) break;
                iterator.remove();
                if (entry.getKey() == null) continue;
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
