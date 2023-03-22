package com.ishland.c2me.base.mixin.priority;

import com.ishland.c2me.base.common.scheduler.ISyncLoadManager;
import com.ishland.c2me.base.common.scheduler.IVanillaChunkManager;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(ServerChunkManager.class)
public abstract class MixinServerChunkManager implements ISyncLoadManager {

    @Shadow
    @Final
    private Thread serverThread;

    @Shadow
    protected abstract boolean isMissingForLevel(@Nullable ChunkHolder holder, int maxLevel);

    @Shadow
    @Nullable
    protected abstract ChunkHolder getChunkHolder(long pos);

    @Shadow @Final public ThreadedAnvilChunkStorage threadedAnvilChunkStorage;
    @Unique
    private volatile ChunkPos currentSyncLoadChunk = null;
    @Unique
    private volatile long syncLoadNanos = 0;

    @Redirect(method = {
            "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;",
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager$MainThreadExecutor;runTasks(Ljava/util/function/BooleanSupplier;)V"), require = 0)
    private void beforeAwaitChunk(ServerChunkManager.MainThreadExecutor instance, BooleanSupplier supplier, int x, int z, ChunkStatus leastStatus, boolean create) {
        if (Thread.currentThread() != this.serverThread || supplier.getAsBoolean()) return;

        this.currentSyncLoadChunk = new ChunkPos(x, z);
        syncLoadNanos = System.nanoTime();
        ((IVanillaChunkManager) this.threadedAnvilChunkStorage).c2me$getSchedulingManager().setCurrentSyncLoad(this.currentSyncLoadChunk);
        instance.runTasks(supplier);
    }

    @Dynamic
    @Inject(method = {
            "getChunkBlocking(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager$MainThreadExecutor;runTasks(Ljava/util/function/BooleanSupplier;)V", shift = At.Shift.BEFORE), require = 0)
    private void beforeAwaitChunkLithium(int x, int z, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir) {
        if (Thread.currentThread() != this.serverThread) return;

        this.currentSyncLoadChunk = new ChunkPos(x, z);
        syncLoadNanos = System.nanoTime();
        ((IVanillaChunkManager) this.threadedAnvilChunkStorage).c2me$getSchedulingManager().setCurrentSyncLoad(this.currentSyncLoadChunk);
    }

    @Inject(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At("RETURN"))
    private void afterGetChunk(int x, int z, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir) {
        if (Thread.currentThread() != this.serverThread) return;

        if (this.currentSyncLoadChunk != null) {
            this.currentSyncLoadChunk = null;
//            System.out.println("Sync load took %.2fms".formatted((System.nanoTime() - syncLoadNanos) / 1e6));
            ((IVanillaChunkManager) this.threadedAnvilChunkStorage).c2me$getSchedulingManager().setCurrentSyncLoad(null);
        }
    }

    @Override
    public ChunkPos getCurrentSyncLoad() {
        return this.currentSyncLoadChunk;
    }
}
