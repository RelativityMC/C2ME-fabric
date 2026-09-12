package com.ishland.c2me.rewrites.chunksystem.mixin.async_serialization;

import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.Executor;

@Mixin(SerializingRegionBasedStorage.class)
public class MixinSerializingRegionBasedStorage {

    @ModifyArg(method = "loadNbt", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private Executor removeIndirection(Executor executor) {
        return Runnable::run;
    }

}
