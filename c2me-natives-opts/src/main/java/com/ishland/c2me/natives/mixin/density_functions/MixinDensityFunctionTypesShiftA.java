package com.ishland.c2me.natives.mixin.density_functions;

import com.ishland.c2me.base.mixin.access.IDoublePerlinNoiseSampler;
import com.ishland.c2me.natives.common.CompiledDensityFunctionArg;
import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import com.ishland.c2me.natives.common.NativeStruct;
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

@Mixin(DensityFunctionTypes.ShiftA.class)
public abstract class MixinDensityFunctionTypesShiftA implements DensityFunction.Base, CompiledDensityFunctionImpl {

    @Shadow
    @Final
    private @Nullable DensityFunction.Noise offsetNoise;
    @Unique
    private long pointer = 0;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
//        System.out.println("Compiling density function: shift_a %s".formatted(this));
        if (this.offsetNoise != null && this.offsetNoise.noise() != null) {
            this.pointer = NativeInterface.createDFIShiftedAData(
                    false,
                    ((NativeStruct) ((IDoublePerlinNoiseSampler) this.offsetNoise.noise()).getFirstSampler()).getNativePointer(),
                    ((NativeStruct) ((IDoublePerlinNoiseSampler) this.offsetNoise.noise()).getSecondSampler()).getNativePointer(),
                    ((IDoublePerlinNoiseSampler) this.offsetNoise.noise()).getAmplitude()
            );
        } else {
            this.pointer = NativeInterface.createDFIShiftedAData(
                    true,
                    0,
                    0,
                    0
            );
        }
        NativeMemoryTracker.registerAllocatedMemory(this, NativeInterface.SIZEOF_density_function_data + NativeInterface.SIZEOF_dfi_simple_shifted_noise_data, this.pointer);
    }

    @Override
    public void compileIfNeeded(boolean includeParents) {
        // no-op: always compilable
    }

    /**
     * @author ishland
     * @reason use native method
     */
    @Overwrite
    public double sample(NoisePos pos) {
        if (this.offsetNoise == null || this.offsetNoise.noise() == null) return 0.0;
        return NativeInterface.dfiBindingsSingleOp(this.pointer, pos.blockX(), pos.blockY(), pos.blockZ());
    }

    @Override
    public void fill(double[] ds, EachApplier arg) {
        if (this.offsetNoise == null || this.offsetNoise.noise() == null) {
            Arrays.fill(ds, 0.0);
            return;
        }
        if (arg instanceof CompiledDensityFunctionArg dfa && dfa.getDFAPointer() != 0) {
            NativeInterface.dfiBindingsMultiOp(this.pointer, dfa.getDFAPointer(), ds);
        } else {
            Base.super.fill(ds, arg);
        }
    }

    @Override
    public long getDFIPointer() {
        return this.pointer;
    }

    /**
     * @author ishland
     * @reason reduce allocs
     */
    @Overwrite
    public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        final Noise apply = visitor.apply(this.offsetNoise);
        if (apply == this.offsetNoise) return visitor.apply(this);
        return visitor.apply(new DensityFunctionTypes.ShiftA(apply));
    }

}
