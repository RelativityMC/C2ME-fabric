package com.ishland.c2me.opts.scheduling.mixin.task_scheduling;

import com.ishland.c2me.opts.scheduling.common.DuckChunkHolder;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerChunkManager.class)
public abstract class MixinServerChunkManager {

    @Shadow @Nullable protected abstract ChunkHolder getChunkHolder(long pos);

    @Shadow @Final private ServerChunkManager.MainThreadExecutor mainThreadExecutor;

    /**
     * @author ishland
     * @reason reduce scheduling overhead with mainInvokingExecutor
     */
    @Overwrite
    public void onLightUpdate(LightType type, ChunkSectionPos pos) {
        ChunkHolder chunkHolder = this.getChunkHolder(pos.toChunkPos().toLong()); // thread-safe
        if (chunkHolder != null) {
            ((DuckChunkHolder) chunkHolder).c2me$queueLightSectionDirty(type, pos.getSectionY());
            if (((DuckChunkHolder) chunkHolder).c2me$shouldScheduleUndirty()) {
                this.mainThreadExecutor.execute(((DuckChunkHolder) chunkHolder)::c2me$undirtyLight);
            }
        }
    }

}
