package com.ishland.c2me.tests.testmod.mixin;

import com.ishland.c2me.tests.testmod.PreGenTask;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private Map<RegistryKey<World>, ServerWorld> worlds;
    @Shadow private volatile boolean running;

    @Shadow public abstract boolean runTask();

    @Shadow public abstract boolean isRunning();

    private final AtomicBoolean ranTest = new AtomicBoolean(false);

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo info) {
        if (ranTest.compareAndSet(false, true)) {
            LOGGER.info("Starting pre-generation task for worlds: {}",
                    String.join(", ",
                            this.worlds.entrySet().stream()
                                    .map(worldEntry -> String.format("%s;%s",
                                            worldEntry.getValue().toString(),
                                            worldEntry.getKey().getValue().toString()))
                                    .collect(Collectors.toSet())
                    ));
            long startTime = System.nanoTime();
            final CompletableFuture<Void> future = CompletableFuture.allOf(this.worlds.values().stream().map(PreGenTask::runPreGen).distinct().toArray(CompletableFuture[]::new));
            while (isRunning() && !future.isDone()) {
                boolean hasTask;
                hasTask = this.runTask();
                for (ServerWorld world : this.worlds.values()) {
                    hasTask = world.getChunkManager().executeQueuedTasks();
                }
                if (!hasTask) LockSupport.parkNanos("waiting for tasks", 100000L);
            }
            long duration = System.nanoTime() - startTime;
            final String message = String.format("PreGen completed after %.1fs", duration / 1_000_000_000.0);
            LOGGER.info(message);
        } else {
            this.running = false;
        }
    }

    @Redirect(method = "loadWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V"))
    private void redirectPrepareStartRegion(MinecraftServer server, WorldGenerationProgressListener worldGenerationProgressListener) {
        LOGGER.info("Not preparing start region");
    }

}
