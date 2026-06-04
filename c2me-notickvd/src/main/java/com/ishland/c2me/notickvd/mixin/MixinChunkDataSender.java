/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
