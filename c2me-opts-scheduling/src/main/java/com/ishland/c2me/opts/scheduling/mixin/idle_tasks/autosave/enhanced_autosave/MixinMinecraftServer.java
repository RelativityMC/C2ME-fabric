package com.ishland.c2me.opts.scheduling.mixin.idle_tasks.autosave.enhanced_autosave;

import com.ishland.c2me.opts.scheduling.common.idle_tasks.IThreadedAnvilChunkStorage;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.ServerTickManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer extends ReentrantThreadExecutor<ServerTask> {

    @Shadow protected abstract boolean shouldKeepTicking();

    @Shadow public abstract Iterable<ServerWorld> getWorlds();

    @Shadow private long tickStartTimeNanos;

    @Shadow @Final private ServerTickManager tickManager;

    public MixinMinecraftServer(String string) {
        super(string);
    }

    @Unique
    private boolean c2me$shouldKeepSavingChunks() {
        return this.hasRunningTasks() || Util.getMeasuringTimeNano() < (this.tickStartTimeNanos - 1_000_000L); // reserve 1ms
    }

    /**
     * @author ishland
     * @reason improve task execution when waiting for next tick
     */
    @ModifyReturnValue(method = "runOneTask", at = @At("RETURN"))
    private boolean postRunTask(boolean original) {
        if (original) return true;
        if (this.c2me$shouldKeepSavingChunks()) {
            for (ServerWorld serverWorld : this.getWorlds()) {
                if (((IThreadedAnvilChunkStorage) serverWorld.getChunkManager().chunkLoadingManager).c2me$runOneChunkAutoSave()) {
                    return true;
                }
            }
        }
        return false;
    }

}
