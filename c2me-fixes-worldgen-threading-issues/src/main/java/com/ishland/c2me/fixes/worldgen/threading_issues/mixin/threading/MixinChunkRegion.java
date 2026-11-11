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

package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.ChunkGenerationStep;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Supplier;

@Mixin(ChunkRegion.class)
public class MixinChunkRegion {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private ChunkGenerationStep generationStep;

    @Shadow
    private @Nullable Supplier<String> currentlyGeneratingStructureName;

    @ModifyVariable(method = "breakBlock", at = @At("HEAD"), argsOnly = true)
    private boolean preventDropItem(final boolean drop, final BlockPos pos, final boolean drop1, final @Nullable Entity breakingEntity, final int maxUpdateDepth) {
        if (drop) {
            LOGGER.error("Detected breakBlock item drop on pos {}, status: {}, currently generating: {}",
                    pos, this.generationStep.targetStatus(), this.currentlyGeneratingStructureName == null ? "unknown": this.currentlyGeneratingStructureName.get());
        }
        return false;
    }

}
