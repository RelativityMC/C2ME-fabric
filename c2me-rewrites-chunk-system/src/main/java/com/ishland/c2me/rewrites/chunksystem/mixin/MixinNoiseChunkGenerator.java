package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.ishland.c2me.base.common.util.InvokingExecutorService;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ExecutorService;

@Mixin(NoiseChunkGenerator.class)
public class MixinNoiseChunkGenerator {

    @Redirect(method = "populateNoise", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMainWorkerExecutor()Ljava/util/concurrent/ExecutorService;"))
    private ExecutorService redirectPopulateNoiseExecutor() {
        return InvokingExecutorService.INSTANCE;
    }

    @Redirect(method = "populateBiomes", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMainWorkerExecutor()Ljava/util/concurrent/ExecutorService;"))
    private ExecutorService redirectBiomeExecutor() {
        return InvokingExecutorService.INSTANCE;
    }

}
