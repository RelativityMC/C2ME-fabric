package com.ishland.c2me.notickvd.mixin;

import com.ishland.c2me.notickvd.common.Config;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.ishland.c2me.rewrites.chunksystem.common.statuses.ServerBlockTicking", remap = false)
public class MixinServerBlockTicking {

    @Dynamic
    @Inject(method = "sendChunkToPlayer(Lcom/ishland/c2me/rewrites/chunksystem/common/ChunkLoadingContext;)V", at = @At("HEAD"), remap = false, cancellable = true)
    private static void preventChunkSending(CallbackInfo ci) {
        if (!Config.ensureChunkCorrectness) {
            ci.cancel();
        }
    }

}
