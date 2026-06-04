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
import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.foreign.Arena;
import java.util.List;

@Mixin(MultiNoiseUtil.Entries.class)
public class MixinMultiNoiseUtilEntries<T> {

    @Shadow @Final private MultiNoiseUtil.SearchTree<T> tree;

    @Unique
    private final Arena c2me$arena = Arena.ofAuto();
    @Unique
    private BindingsTemplate.NativeBiomeSearchTree c2me$nativesMathSearchTree;
    @Unique
    private long c2me$nativesMathSearchTreePtr;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(List<Pair<MultiNoiseUtil.NoiseHypercube, T>> entries, CallbackInfo ci) {
        if (entries.getFirst().getSecond() instanceof RegistryEntry<?> entry /* && entry.value() instanceof Biome */) {
            BindingsTemplate.NativeBiomeSearchTree result =
                    BindingsTemplate.biome_search_tree_node$create(this.c2me$arena, (MultiNoiseUtil.SearchTree<RegistryEntry<Biome>>) this.tree);
            this.c2me$nativesMathSearchTree = result;
            this.c2me$nativesMathSearchTreePtr = result.segment().address();
        }
    }

//    @Inject(method = "getValue(Lnet/minecraft/world/biome/source/util/MultiNoiseUtil$NoiseValuePoint;)Ljava/lang/Object;", at = @At("RETURN"))
//    private void postSample(MultiNoiseUtil.NoiseValuePoint point, CallbackInfoReturnable<T> cir) {
//        if (this.c2me$nativesMathSearchTree != null) {
//            // this.temperatureNoise, this.humidityNoise, this.continentalnessNoise, this.erosionNoise, this.depth, this.weirdnessNoise, 0L
//            int index = Bindings.c2me_natives_biome_search_tree_calc_args(
//                    this.c2me$nativesMathSearchTreePtr,
//                    this.c2me$nativesMathSearchTree.node_c(),
//                    this.c2me$nativesMathSearchTree.tree_depth(),
//                    c2me$toShortSaturating(point.temperatureNoise()),
//                    c2me$toShortSaturating(point.humidityNoise()),
//                    c2me$toShortSaturating(point.continentalnessNoise()),
//                    c2me$toShortSaturating(point.erosionNoise()),
//                    c2me$toShortSaturating(point.depth()),
//                    c2me$toShortSaturating(point.weirdnessNoise()),
//                    (short) 0
//            );
//            if (this.c2me$nativesMathSearchTree.biomes()[index] != cir.getReturnValue()) {
//                throw new IllegalStateException("Biome mismatch: expected " + this.c2me$nativesMathSearchTree.biomes()[index] + ", but got " + cir.getReturnValue());
//            }
//        }
//    }

    @WrapMethod(method = "getValue(Lnet/minecraft/world/biome/source/util/MultiNoiseUtil$NoiseValuePoint;)Ljava/lang/Object;")
    private T postSample(MultiNoiseUtil.NoiseValuePoint point, Operation<T> original) {
        // vanilla code have non-deterministic biome generation, so we can't enable the code for now
//        if (true) original.call(point);

        if (this.c2me$nativesMathSearchTree != null) {
            // this.temperatureNoise, this.humidityNoise, this.continentalnessNoise, this.erosionNoise, this.depth, this.weirdnessNoise, 0L
            int index = Bindings.c2me_natives_biome_search_tree_calc_args(
                    this.c2me$nativesMathSearchTreePtr,
                    this.c2me$nativesMathSearchTree.node_c(),
                    this.c2me$nativesMathSearchTree.tree_depth(),
                    c2me$toShortSaturating(point.temperatureNoise()),
                    c2me$toShortSaturating(point.humidityNoise()),
                    c2me$toShortSaturating(point.continentalnessNoise()),
                    c2me$toShortSaturating(point.erosionNoise()),
                    c2me$toShortSaturating(point.depth()),
                    c2me$toShortSaturating(point.weirdnessNoise()),
                    (short) 0
            );
            return (T) this.c2me$nativesMathSearchTree.biomes()[index];
        }
        return original.call(point);
    }

    @Unique
    private static short c2me$toShortSaturating(long value) {
        if (value < Short.MIN_VALUE) {
            return Short.MIN_VALUE;
        } else if (value > Short.MAX_VALUE) {
            return Short.MAX_VALUE;
        } else {
            return (short) value;
        }
    }

}
