package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.ishland.c2me.base.common.util.InvokingExecutorService;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

@Mixin(ChunkGenerator.class)
public class MixinChunkGenerator {

    @ModifyArg(method = "populateBiomes", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private Executor redirectBiomeExecutor(Executor executor) {
        return Runnable::run;
    }

}
