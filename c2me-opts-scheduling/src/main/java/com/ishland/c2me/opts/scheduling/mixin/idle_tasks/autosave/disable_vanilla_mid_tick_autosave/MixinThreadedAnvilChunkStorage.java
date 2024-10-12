package com.ishland.c2me.opts.scheduling.mixin.idle_tasks.autosave.disable_vanilla_mid_tick_autosave;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BooleanSupplier;

@Mixin(ServerChunkLoadingManager.class)
public class MixinThreadedAnvilChunkStorage {

    @WrapWithCondition(method = "unloadChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager;saveChunks(Ljava/util/function/BooleanSupplier;)V"))
    private boolean stopAutoSavingDuringTick(ServerChunkLoadingManager instance, BooleanSupplier shouldKeepTicking) {
        return false;
    }

}
