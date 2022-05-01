package com.ishland.c2me.natives.mixin.density_functions;

import com.ishland.c2me.base.mixin.access.IDoublePerlinNoiseSampler;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import com.ishland.c2me.natives.common.NativeStruct;
import com.ishland.c2me.natives.common.NativesInterface;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(DensityFunctionTypes.Noise.class)
public abstract class MixinDensityFunctionTypesNoise implements DensityFunction.class_6913 {

    @Shadow
    @Final
    private @Nullable DoublePerlinNoiseSampler noise;

    @Shadow
    @Final
    private @Deprecated double xzScale;

    @Shadow
    @Final
    private double yScale;

    @Unique
    private long dfiPointer = 0;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        System.out.println("Compiling density function: noise %s".formatted(this));
        if (this.noise != null) {
            this.dfiPointer = NativesInterface.createDFINoiseData(
                    false,
                    ((NativeStruct) ((IDoublePerlinNoiseSampler) this.noise).getFirstSampler()).getNativePointer(),
                    ((NativeStruct) ((IDoublePerlinNoiseSampler) this.noise).getSecondSampler()).getNativePointer(),
                    ((IDoublePerlinNoiseSampler) this.noise).getAmplitude(),
                    this.xzScale,
                    this.yScale
            );
        } else {
            this.dfiPointer = NativesInterface.createDFINoiseData(
                    true,
                    0,
                    0,
                    0,
                    this.xzScale,
                    this.yScale
            );
        }
        NativeMemoryTracker.registerAllocatedMemory(this, NativesInterface.SIZEOF_density_function_data + NativesInterface.SIZEOF_dfi_noise_data, this.dfiPointer);
    }

    /**
     * @author ishland
     * @reason use native method
     */
    @Overwrite
    public double sample(DensityFunction.NoisePos pos) {
        if (this.noise == null) return 0.0;
        return NativesInterface.dfiBindingsSingleOp(this.dfiPointer, pos.blockX(), pos.blockY(), pos.blockZ());
    }

    @Override
    public void method_40470(double[] ds, class_6911 arg) {
        if (this.noise == null) {
            Arrays.fill(ds, 0.0);
            return;
        }
        if (arg instanceof NativeStruct nativeStruct) {
            NativesInterface.dfiBindingsMultiOp(this.dfiPointer, nativeStruct.getNativePointer(), ds);
        } else {
            class_6913.super.method_40470(ds, arg);
        }
    }
}
