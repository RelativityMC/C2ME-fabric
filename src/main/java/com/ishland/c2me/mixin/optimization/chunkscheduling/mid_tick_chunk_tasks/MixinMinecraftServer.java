package com.ishland.c2me.mixin.optimization.chunkscheduling.mid_tick_chunk_tasks;

import com.ishland.c2me.common.optimization.chunkscheduling.ServerMidTickTask;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements ServerMidTickTask {

    @Shadow public abstract Iterable<ServerWorld> getWorlds();

    @Shadow @Final private Thread serverThread;
    private static final long minMidTickTaskInterval = 100_000L; // 100us
    private long lastRun = System.nanoTime();

    public void executeTasksMidTick() {
        if (this.serverThread != Thread.currentThread()) return;
        if (System.nanoTime() - lastRun < minMidTickTaskInterval) return;
        for (ServerWorld world : this.getWorlds()) {
            world.chunkManager.mainThreadExecutor.runTask();
        }
        lastRun = System.nanoTime();
    }

}
