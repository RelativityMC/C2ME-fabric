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

package com.ishland.c2me.tests.testmod.mixin.pregen;

import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Structure.class)
public class MixinStructureFeature {

//    @Redirect(method = "locateStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldView;getBiomeAccess()Lnet/minecraft/world/biome/source/BiomeAccess;"), require = 0)
//    private BiomeAccess redirectBiomeAccess(WorldView worldView) {
//        if (worldView instanceof ServerWorld serverWorld) {
//            return new BiomeAccess(serverWorld.getChunkManager().getChunkGenerator().getBiomeSource(), serverWorld.getSeed(), serverWorld.getDimension().getBiomeAccessType());
//        }
//        return worldView.getBiomeAccess();
//    }

}
