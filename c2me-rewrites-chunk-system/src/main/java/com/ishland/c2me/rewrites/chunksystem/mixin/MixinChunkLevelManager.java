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

package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.ishland.c2me.rewrites.chunksystem.common.Config;
import com.ishland.c2me.rewrites.chunksystem.common.ducks.TicketDistanceLevelPropagatorExtension;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelManager;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ThrottledChunkTaskScheduler;
import net.minecraft.server.world.TicketDistanceLevelPropagator;
import net.minecraft.util.thread.TaskExecutor;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.Executor;

@Mixin(value = ChunkLevelManager.class, priority = 1051)
public abstract class MixinChunkLevelManager {

    @Shadow
    @Final
    private TicketDistanceLevelPropagator ticketDistanceLevelPropagator;

    @Shadow
    protected abstract @Nullable ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int i);

    @Dynamic
    @TargetHandler(
            mixin = "com.ishland.vmp.mixins.ticketsystem.ticketpropagator.MixinChunkLevelManager",
            name = "tickTickets"
    )
    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getLevel()I"), require = 0)
    private int fakeLevel(ChunkHolder instance) {
        return Integer.MAX_VALUE;
    }

    @Dynamic
    @TargetHandler(
            mixin = "com.ishland.vmp.mixins.ticketsystem.ticketpropagator.MixinChunkLevelManager",
            name = "tickTickets"
    )
    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkLevelManager;getChunkHolder(J)Lnet/minecraft/server/world/ChunkHolder;"), require = 0)
    private ChunkHolder fakeLevel(ChunkLevelManager instance, long l) {
        return null;
    }

    @Dynamic
    @TargetHandler(
            mixin = "com.ishland.vmp.mixins.ticketsystem.ticketpropagator.MixinChunkLevelManager",
            name = "tickTickets"
    )
    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ChunkLevels;INACCESSIBLE:I", opcode = Opcodes.GETSTATIC, ordinal = 0), require = 0)
    private int fakeLevel() {
        return Integer.MAX_VALUE - 1;
    }

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/util/thread/TaskExecutor;Ljava/util/concurrent/Executor;I)Lnet/minecraft/server/world/ThrottledChunkTaskScheduler;"))
    private ThrottledChunkTaskScheduler syncPlayerTickets(TaskExecutor<Runnable> executor, Executor dispatchExecutor, int maxConcurrentChunks, Operation<ThrottledChunkTaskScheduler> original) {
        if (Config.syncPlayerTickets) {
            return original.call(executor, (Executor) Runnable::run, maxConcurrentChunks); // improve player ticket consistency
        } else {
            return original.call(executor, dispatchExecutor, maxConcurrentChunks);
        }
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/TicketDistanceLevelPropagator;update(I)I", shift = At.Shift.AFTER))
    private void postTicketPropagator(ServerChunkLoadingManager chunkLoadingManager, CallbackInfoReturnable<Boolean> cir) {
        if (this.ticketDistanceLevelPropagator != null) { // ignore if replaced
            Long2IntLinkedOpenHashMap updates = ((TicketDistanceLevelPropagatorExtension) this.ticketDistanceLevelPropagator).c2me$getTicketLevelUpdates();
            while (!updates.isEmpty()) {
                long pos = updates.firstLongKey();
                int level = updates.removeFirstInt();
                this.setLevel(pos, level, null, Integer.MAX_VALUE - 1); // holder and old level is ignored
            }
        }
    }

}
