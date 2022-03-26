package com.ishland.c2me.threading.worldgen.mixin;

import com.ishland.c2me.base.common.util.InvokingExecutorService;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ExecutorService;

@Mixin(ChunkGenerator.class)
public class MixinChunkGenerator {

    @Redirect(method = "populateBiomes", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMainWorkerExecutor()Ljava/util/concurrent/ExecutorService;"))
    private ExecutorService redirectBiomeExecutor() {
        return InvokingExecutorService.INSTANCE;
    }

}
