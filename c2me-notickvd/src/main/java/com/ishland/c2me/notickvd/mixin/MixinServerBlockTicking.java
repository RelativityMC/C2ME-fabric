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
import com.ishland.c2me.rewrites.chunksystem.common.statuses.ServerBlockTicking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = ServerBlockTicking.class, remap = false)
public class MixinServerBlockTicking {

    @Inject(method = "sendChunkToPlayer(Lcom/ishland/c2me/rewrites/chunksystem/common/ChunkLoadingContext;)V", at = @At("HEAD"), remap = false, cancellable = true)
    private static void preventChunkSending(CallbackInfo ci) {
        if (!Config.ensureChunkCorrectness) {
            ci.cancel();
        }
    }

}
