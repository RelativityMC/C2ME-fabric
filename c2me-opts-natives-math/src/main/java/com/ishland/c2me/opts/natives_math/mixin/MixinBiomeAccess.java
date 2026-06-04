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

package com.ishland.c2me.opts.natives_math.mixin;

import com.ishland.c2me.opts.natives_math.common.Bindings;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BiomeAccess.class)
public class MixinBiomeAccess {

    @Shadow @Final private long seed;

    @Shadow @Final private BiomeAccess.Storage storage;

    /**
     * @author ishland
     * @reason replace impl
     */
    @Overwrite
    public RegistryEntry<Biome> getBiome(BlockPos pos) {
        int mask = Bindings.c2me_natives_biome_access_sample(this.seed, pos.getX(), pos.getY(), pos.getZ());

        return this.storage.getBiomeForNoiseGen(
                ((pos.getX() - 2) >> 2) + ((mask & 4) != 0 ? 1 : 0),
                ((pos.getY() - 2) >> 2) + ((mask & 2) != 0 ? 1 : 0),
                ((pos.getZ() - 2) >> 2) + ((mask & 1) != 0 ? 1 : 0)
        );
    }

}
