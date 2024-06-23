package com.ishland.c2me.opts.scheduling.mixin.task_scheduling;

import com.ishland.c2me.opts.scheduling.common.DuckChunkHolder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ServerChunkManager.class)
public abstract class MixinServerChunkManager {

    @Shadow @Nullable protected abstract ChunkHolder getChunkHolder(long pos);

    @Shadow @Final private ServerChunkManager.MainThreadExecutor mainThreadExecutor;

    @Shadow @Final public ServerChunkLoadingManager chunkLoadingManager;

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

    private final AtomicBoolean tickingTicket = new AtomicBoolean(false);

    @WrapOperation(method = "updateChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkTicketManager;update(Lnet/minecraft/server/world/ServerChunkLoadingManager;)Z"))
    private boolean protectTicketReEntrance(ChunkTicketManager instance, ServerChunkLoadingManager chunkLoadingManager, Operation<Boolean> original) {
        if (!this.tickingTicket.compareAndSet(false, true)) {
            final ConcurrentModificationException exception = new ConcurrentModificationException("Re-entrance of ticking ticket");
            exception.printStackTrace();
            throw exception;
        }
        try {
            return original.call(instance, chunkLoadingManager);
        } finally {
            final boolean b = this.tickingTicket.compareAndSet(true, false);
            if (!b) {
                final ConcurrentModificationException exception = new ConcurrentModificationException("What");
                exception.printStackTrace();
                throw exception;
            }
        }
    }

}
