package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.gen.BytecodeGen;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
        original.call(instance, new NoiseRouter(
                BytecodeGen.compile(value.barrierNoise()),
                BytecodeGen.compile(value.fluidLevelFloodednessNoise()),
                BytecodeGen.compile(value.fluidLevelSpreadNoise()),
                BytecodeGen.compile(value.lavaNoise()),
                BytecodeGen.compile(value.temperature()),
                BytecodeGen.compile(value.vegetation()),
                BytecodeGen.compile(value.continents()),
                BytecodeGen.compile(value.erosion()),
                BytecodeGen.compile(value.depth()),
                BytecodeGen.compile(value.ridges()),
                BytecodeGen.compile(value.initialDensityWithoutJaggedness()),
                BytecodeGen.compile(value.finalDensity()),
                BytecodeGen.compile(value.veinToggle()),
                BytecodeGen.compile(value.veinRidged()),
                BytecodeGen.compile(value.veinGap())
        ));
    }

}
