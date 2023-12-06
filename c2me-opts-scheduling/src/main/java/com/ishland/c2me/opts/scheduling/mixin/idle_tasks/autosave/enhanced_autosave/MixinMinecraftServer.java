package com.ishland.c2me.opts.scheduling.mixin.idle_tasks.autosave.enhanced_autosave;

import com.ishland.c2me.opts.scheduling.common.idle_tasks.IThreadedAnvilChunkStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer extends ReentrantThreadExecutor<ServerTask> {

    @Shadow protected abstract boolean shouldKeepTicking();

    @Shadow public abstract Iterable<ServerWorld> getWorlds();

    @Shadow private long tickStartTimeNanos;

    public MixinMinecraftServer(String string) {
        super(string);
    }

    @Unique
    private boolean c2me$shouldKeepSavingChunks() {
        return this.hasRunningTasks() || Util.getMeasuringTimeNano() < this.tickStartTimeNanos;
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

            if (!hasWork && this.c2me$shouldKeepSavingChunks()) {
                for (ServerWorld serverWorld : this.getWorlds()) {
                    if (this.c2me$shouldKeepSavingChunks()) {
                        hasWork |= ((IThreadedAnvilChunkStorage) serverWorld.getChunkManager().threadedAnvilChunkStorage).c2me$runOneChunkAutoSave();
                    }
                }
            }

            return hasWork;
        }
    }

}
