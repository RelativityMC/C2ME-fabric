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

import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.RandomizedIntBlockStateProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RandomizedIntBlockStateProvider.class)
public abstract class MixinRandomizedIntBlockStateProvider {

    @Shadow @Nullable private IntProperty property;

    @Shadow @Final private RegistryEntry<BlockStateProvider> source;

    @Shadow
    @Nullable
    private static IntProperty getIntPropertyByName(BlockState state, String propertyName) {
        throw new AbstractMethodError();
    }

    @Shadow @Final private String propertyName;

    @Shadow @Final private IntProvider values;

    /**
     * @author ishland
     * @reason ensure proper behavior
     */
    @Overwrite
    public BlockState get(final WorldAccess worldAccess, final Random random, final BlockPos pos) {
        BlockState unmodifiedState = this.source.value().get(worldAccess, random, pos);
        IntProperty propertyLocal = this.property;
        if (propertyLocal == null || !unmodifiedState.contains(propertyLocal)) {
            IntProperty property = getIntPropertyByName(unmodifiedState, this.propertyName);
            if (property == null) {
                return unmodifiedState;
            }

            propertyLocal = this.property = property;
        }

        return unmodifiedState.with(propertyLocal, this.values.get(random));
    }

}
