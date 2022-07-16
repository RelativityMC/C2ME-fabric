package com.ishland.c2me.opts.worldgen.vanilla.mixin.noise;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ChunkNoiseSampler.class)
public class MixinChunkNoiseSampler {

    @Mutable
    @Shadow @Final private Map<DensityFunction, DensityFunction> actualDensityFunctionCache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.actualDensityFunctionCache = new Reference2ReferenceOpenHashMap<>(this.actualDensityFunctionCache);
    }

}
