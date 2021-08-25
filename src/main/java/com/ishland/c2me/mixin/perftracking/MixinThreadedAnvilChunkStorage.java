package com.ishland.c2me.mixin.perftracking;

import com.ishland.c2me.common.perftracking.IntegerRollingAverage;
import com.ishland.c2me.common.perftracking.PerfTrackingObject;
import com.ishland.c2me.common.perftracking.PerfTrackingRegistry;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Mixin(value = ThreadedAnvilChunkStorage.class, priority = 1200)
public class MixinThreadedAnvilChunkStorage implements PerfTrackingObject.PerfTrackingTACS {

    @Shadow
    @Final
    ServerWorld world;

    private AtomicLong totalTasks;
    private AtomicLong completedTasks;
    private ScheduledFuture<?> scheduledFuture;
    private IntegerRollingAverage average5s;
    private IntegerRollingAverage average10s;
    private IntegerRollingAverage average1m;
    private IntegerRollingAverage average5m;
    private IntegerRollingAverage average15m;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.totalTasks = new AtomicLong(0L);
        this.completedTasks = new AtomicLong(0L);
        average5s = new IntegerRollingAverage(5 * 2);
        average10s = new IntegerRollingAverage(10 * 2);
        average1m = new IntegerRollingAverage(60 * 2);
        average5m = new IntegerRollingAverage(5 * 60 * 2);
        average15m = new IntegerRollingAverage(15 * 60 * 2);
        this.scheduledFuture = IntegerRollingAverage.SCHEDULER.scheduleAtFixedRate(this::submitAverage, 500, 500, TimeUnit.MILLISECONDS);
        PerfTrackingRegistry.TACs.add(this);
    }

    private void submitAverage() {
        final long totalTasks = this.totalTasks.get();
        final long completedTasks = this.completedTasks.get();
        final int executorLoad = (int) (totalTasks - completedTasks);
        average5s.submit(executorLoad);
        average10s.submit(executorLoad);
        average1m.submit(executorLoad);
        average5m.submit(executorLoad);
        average15m.submit(executorLoad);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        scheduledFuture.cancel(false);
        PerfTrackingRegistry.TACs.remove(this);
    }

    @Override
    public double getAverage5s() {
        return average5s.average();
    }

    @Override
    public double getAverage10s() {
        return average10s.average();
    }

    @Override
    public double getAverage1m() {
        return average1m.average();
    }

    @Override
    public double getAverage5m() {
        return average5m.average();
    }

    @Override
    public double getAverage15m() {
        return average15m.average();
    }

    @Inject(method = "getChunk", at = @At("RETURN"))
    private void onGetChunk(ChunkHolder holder, ChunkStatus requiredStatus, CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
        this.totalTasks.incrementAndGet();
        cir.getReturnValue().exceptionally(__ -> null).thenRun(this.completedTasks::incrementAndGet);
    }

    @Override
    @Unique
    public Identifier getWorldRegistryKey() {
        return this.world.getRegistryKey().getValue();
    }

}
