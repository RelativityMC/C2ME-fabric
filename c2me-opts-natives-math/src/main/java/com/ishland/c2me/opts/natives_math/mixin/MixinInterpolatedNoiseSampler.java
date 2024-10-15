package com.ishland.c2me.opts.natives_math.mixin;

import com.ishland.c2me.opts.natives_math.common.Bindings;
import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

@Mixin(InterpolatedNoiseSampler.class)
public class MixinInterpolatedNoiseSampler {

    @Unique
    private final Arena c2me$arena = Arena.ofAuto();
    @Unique
    private MemorySegment c2me$samplerData = null;
    @Unique
    private long c2me$samplerDataPtr;

    @Inject(method = "<init>(Lnet/minecraft/util/math/noise/OctavePerlinNoiseSampler;Lnet/minecraft/util/math/noise/OctavePerlinNoiseSampler;Lnet/minecraft/util/math/noise/OctavePerlinNoiseSampler;DDDDD)V", at = @At("RETURN"))
    private void postInit(CallbackInfo ci) {
        this.c2me$samplerData = BindingsTemplate.interpolated_noise_sampler$create(this.c2me$arena, (InterpolatedNoiseSampler) (Object) this);
        this.c2me$samplerDataPtr = this.c2me$samplerData.address();
    }

    /**
     * @author ishland
     * @reason replace impl
     */
    @Overwrite
    public double sample(DensityFunction.NoisePos pos) {
        return Bindings.c2me_natives_noise_interpolated(this.c2me$samplerDataPtr, pos.blockX(), pos.blockY(), pos.blockZ());
    }

}
