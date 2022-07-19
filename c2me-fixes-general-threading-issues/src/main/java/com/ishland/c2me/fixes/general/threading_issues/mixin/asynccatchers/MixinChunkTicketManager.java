package com.ishland.c2me.fixes.general.threading_issues.mixin.asynccatchers;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.world.ChunkTicketManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChunkTicketManager.class, priority = 990)
public class MixinChunkTicketManager {

    @Shadow @Final private static Logger LOGGER;
    @Unique
    private boolean isTicking = false;

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void beforeTick(CallbackInfoReturnable<Boolean> cir) {
        if (this.isTicking) {
            final String className = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net/minecraft/class_3204").replace('/', '.');
            final String methodName = FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", "net/minecraft/class_3204", "method_15892", "(Lnet/minecraft/class_3898;)Z");
            final StackTraceElement[] stackTrace = new Throwable().getStackTrace();

            int seenTimes = 0;
            for (StackTraceElement element : stackTrace) {
                if (element.getClassName().equals(className) && element.getMethodName().equals(methodName))
                    seenTimes ++;
            }

            if (seenTimes > 1) {
                final IllegalStateException exception = new IllegalStateException("Re-entry detected on ChunkTicketManager (for %d times)".formatted(seenTimes));
                LOGGER.error("Exception in ChunkTickManager", exception);
                throw exception;
            } else {
                LOGGER.error("isTicking has lost");
                this.isTicking = false;
            }
        }
        this.isTicking = true;
    }

    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void afterTick(CallbackInfoReturnable<Boolean> cir) {
        this.isTicking = false;
    }

}
