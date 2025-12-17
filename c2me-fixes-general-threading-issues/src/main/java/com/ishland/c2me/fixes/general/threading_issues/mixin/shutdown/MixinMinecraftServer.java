package com.ishland.c2me.fixes.general.threading_issues.mixin.shutdown;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @Unique
    private boolean c2me$finishedSaving = false;

    @WrapOperation(method = "shouldExecuteAsync", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isStopped()Z"))
    private boolean modifyShouldExecuteAsync(MinecraftServer instance, Operation<Boolean> original) {
        return original.call(instance) && this.c2me$finishedSaving;
    }

    @Inject(method = "shutdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;save(ZZZ)Z"))
    private void onFinishedSaving(CallbackInfo ci) {
        this.c2me$finishedSaving = true;
    }

}
