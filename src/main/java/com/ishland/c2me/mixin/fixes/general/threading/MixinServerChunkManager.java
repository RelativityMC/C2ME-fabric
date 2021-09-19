package com.ishland.c2me.mixin.fixes.general.threading;

import com.ibm.asyncutil.locks.AsyncReadWriteLock;
import com.ishland.c2me.common.fixes.general.threading.IChunkTicketManager;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkManager.class)
public class MixinServerChunkManager {

    @Redirect(method = "tick()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkTicketManager;tick(Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;)Z"))
    private boolean redirectTickChunkManager(ChunkTicketManager chunkTicketManager, ThreadedAnvilChunkStorage threadedAnvilChunkStorage) {
        final AsyncReadWriteLock ticketLock = ((IChunkTicketManager) chunkTicketManager).getTicketLock();
        try (final AsyncReadWriteLock.WriteLockToken ignored = ticketLock.acquireWriteLock().toCompletableFuture().join()) {
            return chunkTicketManager.tick(threadedAnvilChunkStorage);
        }
    }

}
