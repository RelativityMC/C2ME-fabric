package com.ishland.c2me.compatibility.mixin.thebumblezone;

import com.telepathicgrunt.bumblezone.world.dimension.BzChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.Executor;

@Mixin(BzChunkGenerator.class)
public class MixinBzChunkGenerator {

    @Redirect(method = "populateNoise", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMainWorkerExecutor()Ljava/util/concurrent/Executor;"))
    private Executor redirectMainWorkerExecutor() {
        return Runnable::run;
    }

}
