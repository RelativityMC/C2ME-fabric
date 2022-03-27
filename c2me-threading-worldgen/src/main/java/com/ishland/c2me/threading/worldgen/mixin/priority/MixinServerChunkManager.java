package com.ishland.c2me.threading.worldgen.mixin.priority;

import com.ishland.c2me.base.common.scheduler.SchedulerThread;
import com.ishland.c2me.threading.worldgen.common.ISyncLoadManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerChunkManager.class)
public class MixinServerChunkManager implements ISyncLoadManager {

    @Shadow @Final private Thread serverThread;
    @Unique
    private volatile ChunkPos currentSyncLoadChunk = null;

    @Inject(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At("HEAD"))
    private void beforeGetChunk(int x, int z, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir) {
        if (Thread.currentThread() != this.serverThread) return;

        this.currentSyncLoadChunk = new ChunkPos(x, z);
        SchedulerThread.INSTANCE.notifyPriorityChange();
    }

    @Inject(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At("RETURN"))
    private void afterGetChunk(int x, int z, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir) {
        if (Thread.currentThread() != this.serverThread) return;

        this.currentSyncLoadChunk = null;
        SchedulerThread.INSTANCE.notifyPriorityChange();
    }

    @Override
    public ChunkPos getCurrentSyncLoad() {
        return this.currentSyncLoadChunk;
    }
}
