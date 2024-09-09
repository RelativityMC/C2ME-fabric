package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.opts.dfc.common.gen.BytecodeGen;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.densityfunction.DensityFunction;
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
    private void postCreate(CallbackInfo ci) {
        Reference2ReferenceMap<DensityFunction, DensityFunction> tempCache = new Reference2ReferenceOpenHashMap<>();
        this.noiseRouter = new NoiseRouter(
                BytecodeGen.compile(this.noiseRouter.barrierNoise(), tempCache),
                BytecodeGen.compile(this.noiseRouter.fluidLevelFloodednessNoise(), tempCache),
                BytecodeGen.compile(this.noiseRouter.fluidLevelSpreadNoise(), tempCache),
                BytecodeGen.compile(this.noiseRouter.lavaNoise(), tempCache),
                BytecodeGen.compile(this.noiseRouter.temperature(), tempCache),
                BytecodeGen.compile(this.noiseRouter.vegetation(), tempCache),
                BytecodeGen.compile(this.noiseRouter.continents(), tempCache),
                BytecodeGen.compile(this.noiseRouter.erosion(), tempCache),
                BytecodeGen.compile(this.noiseRouter.depth(), tempCache),
                BytecodeGen.compile(this.noiseRouter.ridges(), tempCache),
                BytecodeGen.compile(this.noiseRouter.initialDensityWithoutJaggedness(), tempCache),
                BytecodeGen.compile(this.noiseRouter.finalDensity(), tempCache),
                BytecodeGen.compile(this.noiseRouter.veinToggle(), tempCache),
                BytecodeGen.compile(this.noiseRouter.veinRidged(), tempCache),
                BytecodeGen.compile(this.noiseRouter.veinGap(), tempCache)
        );
        this.multiNoiseSampler = new MultiNoiseUtil.MultiNoiseSampler(
                BytecodeGen.compile(this.multiNoiseSampler.temperature(), tempCache),
                BytecodeGen.compile(this.multiNoiseSampler.humidity(), tempCache),
                BytecodeGen.compile(this.multiNoiseSampler.continentalness(), tempCache),
                BytecodeGen.compile(this.multiNoiseSampler.erosion(), tempCache),
                BytecodeGen.compile(this.multiNoiseSampler.depth(), tempCache),
                BytecodeGen.compile(this.multiNoiseSampler.weirdness(), tempCache),
                this.multiNoiseSampler.spawnTarget()
        );
    }

}
