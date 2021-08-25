package com.ishland.c2me.mixin.perftracking;

import com.ishland.c2me.common.perftracking.IntegerRollingAverage;
import com.ishland.c2me.common.perftracking.PerfTracked;
import com.ishland.c2me.common.perftracking.PerfTrackingObject;
import com.ishland.c2me.common.perftracking.PerfTrackingRegistry;
import com.mojang.datafixers.util.Either;
import net.minecraft.util.thread.TaskQueue;
import net.minecraft.world.storage.StorageIoWorker;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Mixin(value = StorageIoWorker.class, priority = 1200)
public abstract class MixinStorageIoWorker implements PerfTrackingObject.PerfTrackingIoWorker {

    @Shadow
    protected abstract <T> CompletableFuture<T> run(Supplier<Either<T, Exception>> task);

    private File directory;

    private AtomicLong totalTasks;
    private AtomicLong completedTasks;
    private ScheduledFuture<?> scheduledFuture;
    private IntegerRollingAverage average5s;
    private IntegerRollingAverage average10s;
    private IntegerRollingAverage average1m;
    private IntegerRollingAverage average5m;
    private IntegerRollingAverage average15m;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(File directory, boolean dsync, String name, CallbackInfo info) {
        this.directory = directory;

        this.totalTasks = new AtomicLong(0L);
        this.completedTasks = new AtomicLong(0L);
        average5s = new IntegerRollingAverage(5 * 2);
        average10s = new IntegerRollingAverage(10 * 2);
        average1m = new IntegerRollingAverage(60 * 2);
        average5m = new IntegerRollingAverage(5 * 60 * 2);
        average15m = new IntegerRollingAverage(15 * 60 * 2);
        this.scheduledFuture = IntegerRollingAverage.SCHEDULER.scheduleAtFixedRate(this::submitAverage, 500, 500, TimeUnit.MILLISECONDS);
        PerfTrackingRegistry.ioWorkers.add(this);
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

    @Override
    public File getDirectory() {
        return directory;
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/RegionBasedStorage;close()V", shift = At.Shift.BEFORE))
    private void onClose(CallbackInfo ci) {
        scheduledFuture.cancel(false);
        PerfTrackingRegistry.ioWorkers.remove(this);
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/StorageIoWorker;run(Ljava/util/function/Supplier;)Ljava/util/concurrent/CompletableFuture;"))
    private <T> CompletableFuture<T> redirectRun(StorageIoWorker storageIoWorker, Supplier<Either<T, Exception>> task) {
        totalTasks.incrementAndGet();
        return this.run(PerfTracked.wrap(task, completedTasks::incrementAndGet));
    }

    @Dynamic("MinecraftDev being dumb unable to get the target class resolved")
    @Redirect(method = "writeRemainingResults", at = @At(value = "NEW", target = "net/minecraft/util/thread/TaskQueue$PrioritizedTask"))
    private TaskQueue.PrioritizedTask redirectPrioritizedTask(int priority, Runnable runnable) {
        totalTasks.incrementAndGet();
        return new TaskQueue.PrioritizedTask(priority, PerfTracked.wrap(runnable, completedTasks::incrementAndGet));
    }

}
