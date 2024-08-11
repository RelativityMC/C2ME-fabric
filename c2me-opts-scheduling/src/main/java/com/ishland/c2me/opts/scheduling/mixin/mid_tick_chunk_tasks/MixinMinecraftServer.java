package com.ishland.c2me.opts.scheduling.mixin.mid_tick_chunk_tasks;

import com.ishland.c2me.base.mixin.access.IServerChunkManager;
import com.ishland.c2me.opts.scheduling.common.Config;
import com.ishland.c2me.opts.scheduling.common.ServerMidTickTask;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements ServerMidTickTask {

    @Shadow public abstract Iterable<ServerWorld> getWorlds();

    @Shadow @Final private Thread serverThread;
    @Unique
    private long midTickChunkTasksLastRun = System.nanoTime();

    public void executeTasksMidTick(ServerWorld world) {
        if (this.serverThread != Thread.currentThread()) return;
        if (System.nanoTime() - midTickChunkTasksLastRun < Config.midTickChunkTasksInterval) return;
        ((ThreadExecutor<Runnable>) ((IServerChunkManager) world.getChunkManager()).getMainThreadExecutor()).runTask();
        midTickChunkTasksLastRun = System.nanoTime();
    }

}
