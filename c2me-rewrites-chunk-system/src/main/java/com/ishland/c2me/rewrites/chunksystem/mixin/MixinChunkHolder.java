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

package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.ishland.c2me.rewrites.chunksystem.common.NewChunkHolderVanillaInterface;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkHolder.class)
public class MixinChunkHolder {

    @Shadow private volatile CompletableFuture<OptionalChunk<WorldChunk>> accessibleFuture;

    @Shadow private volatile CompletableFuture<OptionalChunk<WorldChunk>> tickingFuture;

    @Shadow private volatile CompletableFuture<OptionalChunk<WorldChunk>> entityTickingFuture;

    @Shadow private CompletableFuture<?> levelIncreaseFuture;

    @Shadow private CompletableFuture<?> savingFuture;

    @WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;setLevel(I)V"))
    private boolean noopSetLevel(ChunkHolder instance, int level) {
        //noinspection ConstantValue
        return !((Object) this instanceof NewChunkHolderVanillaInterface);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void failFastIncompatibility(CallbackInfo ci) {
        //noinspection ConstantValue
        if (((Object) this instanceof NewChunkHolderVanillaInterface)) {
            this.accessibleFuture = null;
            this.tickingFuture = null;
            this.entityTickingFuture = null;
            this.levelIncreaseFuture = null;
            this.savingFuture = null;
        }
    }

}
