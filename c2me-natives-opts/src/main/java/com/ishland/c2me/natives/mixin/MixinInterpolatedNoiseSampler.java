package com.ishland.c2me.natives.mixin;

import com.ishland.c2me.natives.common.CompiledDensityFunctionArg;
import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import com.ishland.c2me.natives.common.NativeStruct;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InterpolatedNoiseSampler.class, priority = 1200)
public abstract class MixinInterpolatedNoiseSampler implements DensityFunction.class_6913, CompiledDensityFunctionImpl {

    @Shadow @Final private OctavePerlinNoiseSampler lowerInterpolatedNoise;
    @Shadow @Final private OctavePerlinNoiseSampler upperInterpolatedNoise;
    @Shadow @Final private OctavePerlinNoiseSampler interpolationNoise;
    @Shadow @Final private double xzScale;
    @Shadow @Final private double yScale;
    @Shadow @Final private double xzMainScale;
    @Shadow @Final private double yMainScale;
    @Shadow @Final private int cellWidth;
    @Shadow @Final private int cellHeight;

    @Unique
    private long interpolatedSamplerPointer = 0L;

    @Unique
    private long dfiPointer = 0L;

    @Inject(method = "<init>(Lnet/minecraft/util/math/noise/OctavePerlinNoiseSampler;Lnet/minecraft/util/math/noise/OctavePerlinNoiseSampler;Lnet/minecraft/util/math/noise/OctavePerlinNoiseSampler;Lnet/minecraft/world/gen/chunk/NoiseSamplingConfig;II)V", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.interpolatedSamplerPointer = NativeInterface.createPerlinInterpolatedSamplerData(
                ((NativeStruct) this.lowerInterpolatedNoise).getNativePointer(),
                ((NativeStruct) this.upperInterpolatedNoise).getNativePointer(),
                ((NativeStruct) this.interpolationNoise).getNativePointer(),
                this.xzScale,
                this.yScale,
                this.xzMainScale,
                this.yMainScale,
                this.cellWidth,
                this.cellHeight
        );
        NativeMemoryTracker.registerAllocatedMemory(this, NativeInterface.SIZEOF_interpolated_sampler_data, this.interpolatedSamplerPointer);

        this.dfiPointer = NativeInterface.createDFIOldBlendedNoiseData(this.interpolatedSamplerPointer);
        NativeMemoryTracker.registerAllocatedMemory(this, NativeInterface.SIZEOF_density_function_data, this.dfiPointer);
    }

    /**
     * @author ishland
     * @reason use native method
     */
    @Overwrite
    public double sample(DensityFunction.NoisePos pos) {
        return NativeInterface.perlinSampleInterpolated(this.interpolatedSamplerPointer, pos.blockX(), pos.blockY(), pos.blockZ());
    }

    @Override
    public void method_40470(double[] ds, class_6911 arg) {
        if (arg instanceof CompiledDensityFunctionArg dfa && dfa.getDFAPointer() != 0) {
            NativeInterface.dfiBindingsMultiOp(this.dfiPointer, dfa.getDFAPointer(), ds);
        } else {
            DensityFunction.class_6913.super.method_40470(ds, arg);
        }
    }

    @Override
    public long getDFIPointer() {
        return this.dfiPointer;
    }
}
