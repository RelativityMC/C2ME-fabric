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
