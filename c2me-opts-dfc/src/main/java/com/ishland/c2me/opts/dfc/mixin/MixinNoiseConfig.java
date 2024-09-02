package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.opts.dfc.common.McToAst;
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
                McToAst.wrapVanilla(value.barrierNoise()),
                McToAst.wrapVanilla(value.fluidLevelFloodednessNoise()),
                McToAst.wrapVanilla(value.fluidLevelSpreadNoise()),
                McToAst.wrapVanilla(value.lavaNoise()),
                McToAst.wrapVanilla(value.temperature()),
                McToAst.wrapVanilla(value.vegetation()),
                McToAst.wrapVanilla(value.continents()),
                McToAst.wrapVanilla(value.erosion()),
                McToAst.wrapVanilla(value.depth()),
                McToAst.wrapVanilla(value.ridges()),
                McToAst.wrapVanilla(value.initialDensityWithoutJaggedness()),
                McToAst.wrapVanilla(value.finalDensity()),
                McToAst.wrapVanilla(value.veinToggle()),
                McToAst.wrapVanilla(value.veinRidged()),
                McToAst.wrapVanilla(value.veinGap())
        ));
    }

}
