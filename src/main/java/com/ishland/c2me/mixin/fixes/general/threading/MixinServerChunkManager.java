package com.ishland.c2me.mixin.fixes.general.threading;

import com.ishland.c2me.common.fixes.general.threading.IChunkTicketManager;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.locks.ReentrantReadWriteLock;

@Mixin(ServerChunkManager.class)
public class MixinServerChunkManager {

    @Redirect(method = "tick()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkTicketManager;tick(Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;)Z"))
    private boolean redirectTickChunkManager(ChunkTicketManager chunkTicketManager, ThreadedAnvilChunkStorage threadedAnvilChunkStorage) {
        final ReentrantReadWriteLock ticketLock = ((IChunkTicketManager) chunkTicketManager).getTicketLock();
        ticketLock.writeLock().lock();
        try  {
            return chunkTicketManager.tick(threadedAnvilChunkStorage);
        } finally {
            ticketLock.writeLock().unlock();
        }
    }

}
