package com.ishland.c2me.mixin.optimization.chunkscheduling.idle_tasks;

import com.ishland.c2me.common.optimization.chunkscheduling.idle_tasks.IThreadedAnvilChunkStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer extends ReentrantThreadExecutor<ServerTask> {

    @Shadow protected abstract boolean shouldKeepTicking();

    @Shadow public abstract Iterable<ServerWorld> getWorlds();

    public MixinMinecraftServer(String string) {
        super(string);
    }

    /**
     * @author ishland
     * @reason improve task execution when waiting for next tick
     */
    @Overwrite
    private boolean runOneTask() {
        if (super.runTask()) {
            return true;
        } else {
            boolean hasWork = false;
            if (this.shouldKeepTicking()) {
                for(ServerWorld serverWorld : this.getWorlds()) {
                    if (serverWorld.getChunkManager().executeQueuedTasks()) hasWork = true;
                }
            }

            for (ServerWorld serverWorld : this.getWorlds()) {
                if (this.shouldKeepTicking()) {
                    hasWork = ((IThreadedAnvilChunkStorage) serverWorld.getChunkManager().threadedAnvilChunkStorage).runOneChunkAutoSave();
                }
            }

            return hasWork;
        }
    }

}
