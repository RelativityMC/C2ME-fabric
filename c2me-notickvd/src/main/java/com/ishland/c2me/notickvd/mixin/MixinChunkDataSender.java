/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
