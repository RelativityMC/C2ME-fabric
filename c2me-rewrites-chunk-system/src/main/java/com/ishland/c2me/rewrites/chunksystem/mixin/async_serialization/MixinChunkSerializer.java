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

package com.ishland.c2me.rewrites.chunksystem.mixin.async_serialization;

import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.AsyncSerializationUtil;
import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.ChunkIoMainThreadTaskUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.SerializedChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SerializedChunk.class)
public class MixinChunkSerializer {

    @Shadow @Final private static Logger LOGGER;

    @WrapOperation(method = "convert", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/poi/PointOfInterestStorage;initForPalette(Lnet/minecraft/util/math/ChunkSectionPos;Lnet/minecraft/world/chunk/ChunkSection;)V"))
    private void onPoiStorageInitForPalette(PointOfInterestStorage instance, ChunkSectionPos sectionPos, ChunkSection chunkSection, Operation<Void> original) {
        ChunkIoMainThreadTaskUtils.executeMain(() -> original.call(instance, sectionPos, chunkSection));
    }

    @WrapOperation(method = "fromChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;copy()Lnet/minecraft/world/chunk/ChunkSection;"))
    private static ChunkSection avoidSectionCopyOnUnload(ChunkSection instance, Operation<ChunkSection> original) {
        if (AsyncSerializationUtil.duringUnloadSerialization.isBound()) {
            return instance;
        } else {
            return original.call(instance);
        }
    }

}
