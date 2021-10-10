package com.ishland.c2me.tests.testmod.mixin;

import com.ishland.c2me.tests.testmod.IMinecraftServer;
import com.ishland.c2me.tests.testmod.PreGenTask;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.snooper.SnooperListener;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.management.NotificationEmitter;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer extends ReentrantThreadExecutor<ServerTask> implements SnooperListener, CommandOutput, AutoCloseable, IMinecraftServer {

    @Shadow
    @Final
    static Logger LOGGER;
    @Shadow
    @Final
    private Map<RegistryKey<World>, ServerWorld> worlds;
    @Shadow
    private volatile boolean running;

    public MixinMinecraftServer(String string) {
        super(string);
    }

    @Shadow
    public abstract boolean runTask();

    @Shadow
    public abstract boolean isRunning();

    @Shadow private int ticks;
    private final AtomicBoolean ranTest = new AtomicBoolean(false);

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo info) {
        if (ranTest.compareAndSet(false, true)) {
            System.err.printf("Starting pre-generation task for worlds: %s\n",
                    String.join(", ",
                            this.worlds.entrySet().stream()
                                    .map(worldEntry -> String.format("%s;%s",
                                            worldEntry.getValue().toString(),
                                            worldEntry.getKey().getValue().toString()))
                                    .collect(Collectors.toSet())
                    ));
            long startTime = System.nanoTime();
            PreGenTask.PreGenEventListener eventListener = new PreGenTask.PreGenEventListener();
            final CompletableFuture<Void> future = CompletableFuture.allOf(
                    this.worlds.values().stream()
                            .map((ServerWorld world1) -> PreGenTask.runPreGen(world1, eventListener))
                            .distinct()
                            .toArray(CompletableFuture[]::new)
            );
            eventListener.fullyStarted = true;
            while (!future.isDone() && isRunning()) {
                if (!c2metest$runAsyncTask()) LockSupport.parkNanos("waiting for tasks", 100000L);
            }
            if (!isRunning()) LOGGER.error("Exiting due to server stopping");
            for (ServerWorld world : this.worlds.values()) {
                world.getChunkManager().tick(() -> true);
            }
            long duration = System.nanoTime() - startTime;
            final String message = String.format("PreGen completed after %.1fs", duration / 1_000_000_000.0);
            LOGGER.info(message);
            System.err.print(message + "\n");
        } else {
            this.running = false;
        }
    }

    private volatile long handledGc = -1L;
    private volatile long largeOverheadGC = -2L;

    private void c2metest$handleGC() {
        if (largeOverheadGC == -2L) {
            synchronized (this) {
                if (largeOverheadGC != -2L) return;
                ManagementFactory.getGarbageCollectorMXBeans().stream().filter(obj -> obj instanceof NotificationEmitter).forEach(bean -> {
                    ((NotificationEmitter) bean).addNotificationListener((notification, __unused) -> {
                        if (!notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION))
                            return;
                        final GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
                        final GcInfo gcInfo = info.getGcInfo();
                        final long usedBefore = gcInfo.getMemoryUsageBeforeGc().values().stream().flatMapToLong(memoryUsage -> LongStream.of(memoryUsage.getUsed())).sum();
                        final long usedAfter = gcInfo.getMemoryUsageAfterGc().values().stream().flatMapToLong(memoryUsage -> LongStream.of(memoryUsage.getUsed())).sum();
                        final long total = gcInfo.getMemoryUsageAfterGc().values().stream().flatMapToLong(memoryUsage -> LongStream.of(memoryUsage.getCommitted())).sum();
                        final long max = gcInfo.getMemoryUsageAfterGc().values().stream().flatMapToLong(memoryUsage -> LongStream.of(memoryUsage.getMax())).sum();
                        LOGGER.info(String.format("[noprogress]GC: %s: [%d] %s caused by %s took %dms, %.1fMB -> %.1fMB (%.1fMB committed %.1fMB max)",
                                info.getGcName(), gcInfo.getId(), info.getGcAction(), info.getGcCause(), gcInfo.getDuration(),
                                usedBefore / 1024.0 / 1024.0, usedAfter / 1024.0 / 1024.0, total / 1024.0 / 1024.0, max / 1024.0 / 1024.0));
                        if (gcInfo.getDuration() >= 200) largeOverheadGC = gcInfo.getId();
                    }, null, null);
                });
                largeOverheadGC = -1L;
            }
        }
        final long largeOverheadGC = this.largeOverheadGC;
        if (largeOverheadGC != handledGc || Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) < 256 * 1024 * 1024) {
            // Too spammy I think
            // LOGGER.warn("High GC overhead / low available heap, saving worlds...");
            this.worlds.values().forEach(world -> world.getChunkManager().tick(() -> true));
            this.worlds.values().forEach(world -> world.getChunkManager().save(false));
            this.worlds.values().forEach(world -> world.getChunkManager().threadedAnvilChunkStorage.completeAll());
            handledGc = largeOverheadGC;
        }
    }

    private volatile long lastTick = System.currentTimeMillis();

    @Override
    public boolean c2metest$runAsyncTask() {
        c2metest$handleGC();
        boolean hasTask = false;
        if (System.currentTimeMillis() - lastTick > 50) {
            for (ServerWorld world : this.worlds.values()) {
                world.getChunkManager().tick(() -> true);
                world.getBlockTickScheduler().tick();
                world.getFluidTickScheduler().tick();
            }
            lastTick += 50;
            hasTask = true;
        }
        while (super.runTask()) hasTask = true;
        for (ServerWorld world : this.worlds.values()) {
            while (world.getChunkManager().executeQueuedTasks()) hasTask = true;
        }
        return hasTask;
    }

    @Redirect(method = "loadWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V"))
    private void redirectPrepareStartRegion(MinecraftServer server, WorldGenerationProgressListener worldGenerationProgressListener) {
        LOGGER.info("Not preparing start region");
    }

    @Redirect(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;populateCrashReport(Lnet/minecraft/util/crash/CrashReport;)Lnet/minecraft/util/crash/CrashReport;"))
    private CrashReport redirectRunServerAddSystemDetails(MinecraftServer minecraftServer, CrashReport report) {
//        CrashMemoryReserve.releaseMemory();
        return minecraftServer.populateCrashReport(report);
    }

}
