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
