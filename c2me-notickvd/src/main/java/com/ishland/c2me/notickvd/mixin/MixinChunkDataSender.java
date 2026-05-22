package com.ishland.c2me.notickvd.mixin;

import com.ishland.c2me.notickvd.common.Config;
import net.minecraft.server.network.ChunkDataSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkDataSender.class)
public class MixinChunkDataSender {

    @Shadow
    private float desiredBatchSize;

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static boolean modifyInit(boolean local) {
        return local || Config.chunkSendingSpeedMultiplierPercentage == 0L;
    }

    @Inject(method = "onAcknowledgeChunks", at = @At("RETURN"))
    private void modifyBatchSize(float desiredBatchSize, CallbackInfo ci) {
        if (Config.chunkSendingSpeedMultiplierPercentage > 0L) {
            this.desiredBatchSize *= ((float) Config.chunkSendingSpeedMultiplierPercentage) / 100.0F;
        }
    }

}
