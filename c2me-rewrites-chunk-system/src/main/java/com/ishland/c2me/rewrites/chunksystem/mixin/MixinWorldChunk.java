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

import com.ishland.c2me.rewrites.chunksystem.common.ducks.WorldChunkExtension;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(WorldChunk.class)
public class MixinWorldChunk implements WorldChunkExtension {

    @Shadow @Final private World world;

    @Unique
    private boolean c2me$blockTicking;

    @Override
    public boolean c2me$isBlockTicking() {
        return this.c2me$blockTicking;
    }

    @Override
    public void c2me$setBlockTicking(boolean blockTicking) {
        this.c2me$blockTicking = blockTicking;
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.c2me$blockTicking = true; // for unmanaged chunks
    }

    @WrapMethod(method = "canTickBlockEntity")
    private boolean wrapCanTickBlockEntity(BlockPos pos, Operation<Boolean> original) {
        return (this.world.isClient() || this.c2me$isBlockTicking()) && original.call(pos);
    }

    @ModifyVariable(method = "<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/ProtoChunk;Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;)V", at = @At("HEAD"), argsOnly = true)
    private static WorldChunk.EntityLoader injectEntityLoaderFromProtoChunk(final WorldChunk.EntityLoader entityLoader, final ServerWorld world, final ProtoChunk protoChunk, final WorldChunk.EntityLoader unused) {
        List<BlockEntity> blockEntities = new ArrayList<>(protoChunk.getBlockEntities().values());
        return chunk -> {
            if (entityLoader != null) {
                entityLoader.run(chunk);
            }

            for (BlockEntity blockEntity : blockEntities) {
                chunk.setBlockEntity(blockEntity);
            }
        };
    }

    @Redirect(method = "<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/ProtoChunk;Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ProtoChunk;getBlockEntities()Ljava/util/Map;"))
    private Map<BlockPos, BlockEntity> nukeEarlyBlockEntitiesFromProtoChunk(ProtoChunk instance) {
        return Map.of();
    }

}
