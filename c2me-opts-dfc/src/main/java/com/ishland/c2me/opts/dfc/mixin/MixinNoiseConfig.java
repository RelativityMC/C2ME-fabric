package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.opts.dfc.common.gen.BytecodeGen;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseConfig.class)
public class MixinNoiseConfig {

    @Mutable
    @Shadow @Final private NoiseRouter noiseRouter;

    @WrapOperation(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/noise/NoiseConfig;noiseRouter:Lnet/minecraft/world/gen/noise/NoiseRouter;", opcode = Opcodes.PUTFIELD))
    private void postCreate(NoiseConfig instance, NoiseRouter value, Operation<Void> original) {
        Reference2ReferenceMap<DensityFunction, DensityFunction> tempCache = new Reference2ReferenceOpenHashMap<>();
        original.call(instance, new NoiseRouter(
                BytecodeGen.compile(value.barrierNoise(), tempCache),
                BytecodeGen.compile(value.fluidLevelFloodednessNoise(), tempCache),
                BytecodeGen.compile(value.fluidLevelSpreadNoise(), tempCache),
                BytecodeGen.compile(value.lavaNoise(), tempCache),
                BytecodeGen.compile(value.temperature(), tempCache),
                BytecodeGen.compile(value.vegetation(), tempCache),
                BytecodeGen.compile(value.continents(), tempCache),
                BytecodeGen.compile(value.erosion(), tempCache),
                BytecodeGen.compile(value.depth(), tempCache),
                BytecodeGen.compile(value.ridges(), tempCache),
                BytecodeGen.compile(value.initialDensityWithoutJaggedness(), tempCache),
                BytecodeGen.compile(value.finalDensity(), tempCache),
                BytecodeGen.compile(value.veinToggle(), tempCache),
                BytecodeGen.compile(value.veinRidged(), tempCache),
                BytecodeGen.compile(value.veinGap(), tempCache)
        ));
    }

}
