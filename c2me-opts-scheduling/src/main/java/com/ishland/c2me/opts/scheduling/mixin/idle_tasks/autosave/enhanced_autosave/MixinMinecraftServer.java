/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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

    public MixinMinecraftServer(String name, boolean propagatesCrashes) {
        super(name, propagatesCrashes);
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
                    return false;
                }
            }
        }
        return false;
    }

}
