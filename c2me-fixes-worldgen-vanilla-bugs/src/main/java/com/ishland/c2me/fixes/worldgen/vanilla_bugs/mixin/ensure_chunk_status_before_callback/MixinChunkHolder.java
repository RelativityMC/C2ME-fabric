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

package com.ishland.c2me.fixes.worldgen.vanilla_bugs.mixin.ensure_chunk_status_before_callback;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder {

    @Shadow public abstract boolean isAccessible();

    @Shadow public abstract CompletableFuture<OptionalChunk<WorldChunk>> getTickingFuture();

    @Shadow public abstract CompletableFuture<OptionalChunk<WorldChunk>> getAccessibleFuture();

    @Shadow public abstract CompletableFuture<OptionalChunk<WorldChunk>> getEntityTickingFuture();

    @WrapWithCondition(method = "method_31412", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager;onChunkStatusChange(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/server/world/ChunkLevelType;)V"))
    private boolean ensureChunkStatusBeforeCallback(ServerChunkLoadingManager instance, ChunkPos chunkPos, ChunkLevelType levelType) {
        return switch (levelType) {
            case INACCESSIBLE -> true;
            case FULL -> this.c2me$isStatusReached(this.getAccessibleFuture());
            case BLOCK_TICKING -> this.c2me$isStatusReached(this.getTickingFuture());
            case ENTITY_TICKING -> this.c2me$isStatusReached(this.getEntityTickingFuture());
        };
    }

    @Unique
    private boolean c2me$isStatusReached(CompletableFuture<OptionalChunk<WorldChunk>> future) {
        return future.isDone() && !future.isCompletedExceptionally() && future.join().isPresent();
    }

}
