package com.ishland.c2me.compatibility.mixin.terra;

import com.dfsek.terra.api.math.noise.NoiseSampler;
import com.dfsek.terra.api.math.paralithic.noise.NoiseFunction2;
import com.dfsek.terra.config.loaders.config.sampler.templates.noise.ExpressionFunctionTemplate;
import com.ishland.c2me.compatibility.common.terra.ThreadLocalNoiseFunction2;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExpressionFunctionTemplate.class)
public class MixinExpressionFunctionTemplate {

    @Dynamic
    @Redirect(method = "lambda$generateFunctions$0", at = @At(value = "NEW", target = "com/dfsek/terra/api/math/paralithic/noise/NoiseFunction2"), remap = false)
    private static NoiseFunction2 redirectNoiseFunction2(NoiseSampler gen) {
        return new ThreadLocalNoiseFunction2(gen);
    }

}
