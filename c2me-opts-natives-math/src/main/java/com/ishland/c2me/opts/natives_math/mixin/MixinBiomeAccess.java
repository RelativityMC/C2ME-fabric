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

package com.ishland.c2me.opts.natives_math.mixin;

import com.ishland.c2me.opts.natives_math.common.Bindings;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSupplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BiomeAccess.class)
public class MixinBiomeAccess {

    @Shadow @Final private long seed;

    @Shadow @Final private BiomeSupplier storage;

    /**
     * @author ishland
     * @reason replace impl
     */
    @Overwrite
    public RegistryEntry<Biome> getBiome(final int x, final int y, final int z) {
        int mask = Bindings.c2me_natives_biome_access_sample(this.seed, x, y, z);

        return this.storage.getBiomeForNoiseGen(
                ((x - 2) >> 2) + ((mask & 4) != 0 ? 1 : 0),
                ((y - 2) >> 2) + ((mask & 2) != 0 ? 1 : 0),
                ((z - 2) >> 2) + ((mask & 1) != 0 ? 1 : 0)
        );
    }

}
