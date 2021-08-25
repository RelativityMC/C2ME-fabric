package com.ishland.c2me.mixin.perftracking;

import com.ishland.c2me.common.perftracking.IntegerRollingAverage;
import com.ishland.c2me.common.perftracking.PerfTrackingObject;
import com.ishland.c2me.common.perftracking.PerfTrackingRegistry;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ThreadExecutor.class)
public abstract class MixinThreadExecutor<R extends Runnable> implements PerfTrackingObject.PerfTrackingThreadExecutor {

    @Shadow public abstract String getName();

    @Shadow protected abstract Thread getThread();

    @Shadow @Final private Queue<R> tasks;
    private ScheduledFuture<?> scheduledFuture;
    private IntegerRollingAverage average5s;
    private IntegerRollingAverage average10s;
    private IntegerRollingAverage average1m;
    private IntegerRollingAverage average5m;
    private IntegerRollingAverage average15m;

    private AtomicInteger executingTaskCount;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        average5s = new IntegerRollingAverage(5 * 2);
        average10s = new IntegerRollingAverage(10 * 2);
        average1m = new IntegerRollingAverage(60 * 2);
        average5m = new IntegerRollingAverage(5 * 60 * 2);
        average15m = new IntegerRollingAverage(15 * 60 * 2);
        this.scheduledFuture = IntegerRollingAverage.SCHEDULER.scheduleAtFixedRate(this::submitAverage, 500, 500, TimeUnit.MILLISECONDS);
        this.executingTaskCount = new AtomicInteger(0);
        PerfTrackingRegistry.threadExecutors.add(this);
    }

    private void submitAverage() {
        final int executorLoad = this.tasks.size() + this.executingTaskCount.get();
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
    public String getExecutorName() {
        return this.getName();
    }

    @Override
    public String getThreadName() {
        return this.getThread().getName();
    }

    @Inject(method = "runTask", at = @At("HEAD"))
    private void beforeRunTask(CallbackInfoReturnable<Boolean> cir) {
        this.executingTaskCount.incrementAndGet();
    }

    @Inject(method = "runTask", at = @At("RETURN"))
    private void afterRunTask(CallbackInfoReturnable<Boolean> cir) {
        this.executingTaskCount.decrementAndGet();
    }
}
