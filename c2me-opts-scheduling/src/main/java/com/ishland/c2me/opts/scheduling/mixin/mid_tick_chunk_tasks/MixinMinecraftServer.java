package com.ishland.c2me.opts.scheduling.mixin.mid_tick_chunk_tasks;

import com.ishland.c2me.base.mixin.access.IServerChunkManager;
import com.ishland.c2me.opts.scheduling.common.ServerMidTickTask;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.thread.ThreadExecutor;
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
            ((ThreadExecutor<Runnable>) ((IServerChunkManager) world.getChunkManager()).getMainThreadExecutor()).runTask();
        }
        lastRun = System.nanoTime();
    }

}
