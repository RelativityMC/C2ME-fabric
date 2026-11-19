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

package com.ishland.c2me.opts.dfc.mixin;

import com.google.common.base.Stopwatch;
import com.ishland.c2me.opts.dfc.common.ast.opto.OptoPasses;
import com.ishland.c2me.opts.dfc.common.ducks.NoiseRouterExtension;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NoiseConfig.class, priority = 900)
public class MixinNoiseConfig {

    @Mutable
    @Shadow @Final private NoiseRouter noiseRouter;

    @Mutable
    @Shadow @Final private MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postCreate(ChunkGeneratorSettings chunkGeneratorSettings, RegistryEntryLookup<DoublePerlinNoiseSampler.NoiseParameters> noiseParametersLookup, long seed, CallbackInfo ci) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Reference2ReferenceMap<DensityFunction, OptoPasses.AstPair> optoCache = new Reference2ReferenceOpenHashMap<>();
        Reference2ReferenceMap<DensityFunction, DensityFunction> tempCache = new Reference2ReferenceOpenHashMap<>();
        DensityFunction finalFinalDensity = DensityFunctionTypes.add(this.noiseRouter.finalDensity(), DensityFunctionTypes.Beardifier.INSTANCE);
        NoiseRouter original = this.noiseRouter;
        ((NoiseRouterExtension) (Object) original).c2me$setFinalFinalDensity(finalFinalDensity);
        BytecodeGen.Context genContext = BytecodeGen.initContext();
        this.noiseRouter = new NoiseRouter(
                genContext.compileDelayed("barrier", this.noiseRouter.barrierNoise()),
                genContext.compileDelayed("fluid_level_floodedness", this.noiseRouter.fluidLevelFloodednessNoise()),
                genContext.compileDelayed("fluid_level_spread", this.noiseRouter.fluidLevelSpreadNoise()),
                genContext.compileDelayed("lava", this.noiseRouter.lavaNoise()),
                genContext.compileDelayed("temperature", this.noiseRouter.temperature()),
                genContext.compileDelayed("vegetation", this.noiseRouter.vegetation()),
                genContext.compileDelayed("continents", this.noiseRouter.continents()),
                genContext.compileDelayed("erosion", this.noiseRouter.erosion()),
                genContext.compileDelayed("depth", this.noiseRouter.depth()),
                genContext.compileDelayed("ridges", this.noiseRouter.ridges()),
                genContext.compileDelayed("preliminary_surface_level", this.noiseRouter.preliminarySurfaceLevel()),
                genContext.compileDelayed("final_density", this.noiseRouter.finalDensity()),
                genContext.compileDelayed("vein_toggle", this.noiseRouter.veinToggle()),
                genContext.compileDelayed("vein_ridged", this.noiseRouter.veinRidged()),
                genContext.compileDelayed("vein_gap", this.noiseRouter.veinGap())
        );
        ((NoiseRouterExtension) (Object) this.noiseRouter).c2me$setFinalFinalDensity(
                genContext.compileDelayed("final_final_density", finalFinalDensity)
        );
        ((NoiseRouterExtension) (Object) this.noiseRouter).c2me$setOriginalNoiseRouter(original);
        this.multiNoiseSampler = new MultiNoiseUtil.MultiNoiseSampler(
                genContext.compileDelayed("multiNoiseSampler_temperature", this.multiNoiseSampler.temperature()),
                genContext.compileDelayed("multiNoiseSampler_vegetation", this.multiNoiseSampler.humidity()),
                genContext.compileDelayed("multiNoiseSampler_continents", this.multiNoiseSampler.continentalness()),
                genContext.compileDelayed("multiNoiseSampler_erosion", this.multiNoiseSampler.erosion()),
                genContext.compileDelayed("multiNoiseSampler_depth", this.multiNoiseSampler.depth()),
                genContext.compileDelayed("multiNoiseSampler_ridges", this.multiNoiseSampler.weirdness()),
                this.multiNoiseSampler.spawnTarget()
        );
        BytecodeGen.finalizeCompilation(genContext);
        stopwatch.stop();
        System.out.println(String.format("Density function compilation finished in %s", stopwatch));
    }

}
