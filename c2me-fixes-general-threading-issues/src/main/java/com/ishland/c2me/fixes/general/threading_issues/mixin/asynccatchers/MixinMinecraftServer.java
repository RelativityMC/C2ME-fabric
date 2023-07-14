package com.ishland.c2me.fixes.general.threading_issues.mixin.asynccatchers;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ConcurrentModificationException;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @Shadow @Final private Thread serverThread;

    @Inject(method = "save", at = @At("HEAD"))
    private void preventAsyncSave(CallbackInfoReturnable<Boolean> cir) {
        if (Thread.currentThread() != this.serverThread) {
            final ConcurrentModificationException exception = new ConcurrentModificationException("Attempted to call MinecraftServer#save async");
            exception.printStackTrace();
            throw exception;
        }
    }

    @Inject(method = "saveAll", at = @At("HEAD"))
    private void preventAsyncSaveAll(CallbackInfoReturnable<Boolean> cir) {
        if (Thread.currentThread() != this.serverThread) {
            final ConcurrentModificationException exception = new ConcurrentModificationException("Attempted to call MinecraftServer#saveAll async");
            exception.printStackTrace();
            throw exception;
        }
    }

}
