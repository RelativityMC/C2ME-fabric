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
