package com.ishland.c2me.tests.testmod.mixin;

import com.ishland.c2me.tests.testmod.PreGenTask;
import com.sun.management.GcInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.crash.CrashMemoryReserve;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @Shadow
    @Final
    static Logger LOGGER;
    @Shadow
    @Final
    private Map<RegistryKey<World>, ServerWorld> worlds;
    @Shadow
    private volatile boolean running;

    @Shadow
    public abstract boolean runTask();

    @Shadow
    public abstract boolean isRunning();

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
            AtomicLong lastTick = new AtomicLong(System.currentTimeMillis());
            while (!future.isDone() && isRunning()) {
                test$handleGC();
                boolean doTick = System.currentTimeMillis() - lastTick.get() > 50L;
                boolean hasTask = doTick;
                if (this.runTask()) hasTask = true;
                for (ServerWorld world : this.worlds.values()) {
                    if (world.getChunkManager().executeQueuedTasks()) hasTask = true;
                    if (doTick) world.getChunkManager().tick(() -> true);
                }
                if (doTick) lastTick.set(System.currentTimeMillis());
                if (!hasTask) LockSupport.parkNanos("waiting for tasks", 100000L);
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

    private long handledGc = -1L;

    private void test$handleGC() {
        final Optional<GarbageCollectorMXBean> optional = ManagementFactory.getGarbageCollectorMXBeans().stream().filter(obj -> obj instanceof com.sun.management.GarbageCollectorMXBean).findAny();
        optional.ifPresent(garbageCollectorMXBean -> {
            final GcInfo lastGcInfo = ((com.sun.management.GarbageCollectorMXBean) garbageCollectorMXBean).getLastGcInfo();
            if (handledGc == lastGcInfo.getId()) return;
            handledGc = lastGcInfo.getId();
            if (lastGcInfo.getDuration() > 200) {
                LOGGER.warn("High GC overhead, saving worlds...");
                this.worlds.values().forEach(world -> {
                    world.getChunkManager().tick(() -> true);
                    world.getChunkManager().save(false);
                });
                this.worlds.values().forEach(world -> world.getChunkManager().threadedAnvilChunkStorage.completeAll());
            }
        });
    }

    @Redirect(method = "loadWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V"))
    private void redirectPrepareStartRegion(MinecraftServer server, WorldGenerationProgressListener worldGenerationProgressListener) {
        LOGGER.info("Not preparing start region");
    }

    @Redirect(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;addSystemDetails(Lnet/minecraft/util/SystemDetails;)Lnet/minecraft/util/SystemDetails;"))
    private SystemDetails redirectRunServerAddSystemDetails(MinecraftServer server, SystemDetails details) {
        CrashMemoryReserve.releaseMemory();
        return server.addSystemDetails(details);
    }

}
