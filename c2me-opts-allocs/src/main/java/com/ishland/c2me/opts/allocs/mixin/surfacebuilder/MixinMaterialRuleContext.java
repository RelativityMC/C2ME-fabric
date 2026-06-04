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

package com.ishland.c2me.opts.allocs.mixin.surfacebuilder;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(MaterialRules.MaterialRuleContext.class)
public class MixinMaterialRuleContext {

    @Shadow
    @Final
    private Function<BlockPos, RegistryEntry<Biome>> posToBiome;

    @Shadow
    @Final
    private BlockPos.Mutable pos;

    @Shadow
    private long uniquePosValue;

    @Shadow
    private Supplier<RegistryEntry<Biome>> biomeSupplier;

    @Shadow
    private int blockY;

    @Shadow
    private int fluidHeight;

    @Shadow
    private int stoneDepthBelow;

    @Shadow
    private int stoneDepthAbove;

    @Unique
    private int lazyPosX;
    @Unique
    private int lazyPosY;
    @Unique
    private int lazyPosZ;
    @Unique
    private RegistryEntry<Biome> lastBiome = null;
    @Unique
    private RegistryKey<Biome> lastBiomeKey = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.biomeSupplier = () -> {
            if (this.lastBiome == null)
                return this.lastBiome = this.posToBiome.apply(this.pos.set(this.lazyPosX, this.lazyPosY, this.lazyPosZ));
            return this.lastBiome;
        };
    }

    /**
     * @author ishland
     * @reason reduce allocs
     */
    @Overwrite
    public void initVerticalContext(int i, int j, int k, int l, int m, int n) {
        // TODO [VanillaCopy]
        ++this.uniquePosValue;
        this.blockY = m;
        this.fluidHeight = k;
        this.stoneDepthBelow = j;
        this.stoneDepthAbove = i;

        // set lazy values
        this.lazyPosX = l;
        this.lazyPosY = m;
        this.lazyPosZ = n;
        // clear cache
        this.lastBiome = null;
        this.lastBiomeKey = null;
    }

}
