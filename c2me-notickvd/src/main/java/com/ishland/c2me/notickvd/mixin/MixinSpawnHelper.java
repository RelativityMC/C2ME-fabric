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

import com.ishland.c2me.base.common.util.FilteringIterable;
import com.ishland.c2me.base.mixin.access.IChunkLevelManager;
import com.ishland.c2me.base.mixin.access.ISimulationDistanceLevelPropagator;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpawnHelper.class)
public class MixinSpawnHelper {

    @WrapOperation(method = "setupSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;iterateEntities()Ljava/lang/Iterable;"))
    private static Iterable<Entity> redirectIterateEntities(ServerWorld serverWorld, Operation<Iterable<Entity>> op) {
        Long2ByteMap trackedChunks = ((ISimulationDistanceLevelPropagator) ((IChunkLevelManager) ((IThreadedAnvilChunkStorage) serverWorld.getChunkManager().chunkLoadingManager).getLevelManager()).getSimulationDistanceLevelPropagator()).getLevels();
        return new FilteringIterable<>(op.call(serverWorld), entity -> trackedChunks.containsKey(entity.getChunkPos().toLong()));
    }

}
