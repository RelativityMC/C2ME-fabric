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

package com.ishland.c2me.threading.lighting.mixin;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.world.ChunkTaskScheduler;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.thread.SimpleConsecutiveExecutor;
import net.minecraft.util.thread.TaskExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Mixin(ServerChunkLoadingManager.class)
public class MixinThreadedAnvilChunkStorage {

    @Shadow
    @Final
    private ServerWorld world;
    private ExecutorService lightThread = null;

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "(Ljava/util/concurrent/Executor;Ljava/lang/String;)Lnet/minecraft/util/thread/SimpleConsecutiveExecutor;"))
    private SimpleConsecutiveExecutor onLightExecutorInit(Executor executor, String name, Operation<SimpleConsecutiveExecutor> original) {
        if (!name.equals("light")) return original.call(executor, name);
        lightThread = new ThreadPoolExecutor(
                1, 1,
                0, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setPriority(Thread.NORM_PRIORITY - 1).setDaemon(true).setNameFormat(String.format("%s - Light", world.getRegistryKey().getValue().toUnderscoreSeparatedString())).build()
        );
        return original.call(lightThread, name);
    }

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/util/thread/TaskExecutor;Ljava/util/concurrent/Executor;)Lnet/minecraft/server/world/ChunkTaskScheduler;"))
    private ChunkTaskScheduler onLightExecutorInit1(TaskExecutor<?> arg, Executor executor, Operation<ChunkTaskScheduler> original) {
        if (!arg.getName().equals("light")) original.call(arg, executor);
        return original.call(arg, lightThread);
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void afterClose(CallbackInfo info) {
        lightThread.shutdown();
    }

}
