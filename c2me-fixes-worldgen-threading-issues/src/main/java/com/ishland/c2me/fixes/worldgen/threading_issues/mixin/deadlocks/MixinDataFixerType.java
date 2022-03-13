package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.deadlocks;

import com.ishland.c2me.base.common.util.CFUtil;
import com.mojang.datafixers.types.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;

@Mixin(Type.class)
public class MixinDataFixerType {

    @Redirect(method = "rewrite", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;join()Ljava/lang/Object;"), remap = false)
    private <T> T redirectJoin(CompletableFuture<T> completableFuture) {
        return CFUtil.join(completableFuture);
    }

}
