package com.ishland.c2me.natives.mixin.density_functions;

import com.google.common.collect.ImmutableMap;
import com.ishland.c2me.base.mixin.access.IDoublePerlinNoiseSampler;
import com.ishland.c2me.natives.common.CompiledDensityFunctionArg;
import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import com.ishland.c2me.natives.common.DensityFunctionUtils;
import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import com.ishland.c2me.natives.common.NativeStruct;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(DensityFunctionTypes.ShiftedNoise.class)
public abstract class MixinDensityFunctionTypesShiftedNoise implements DensityFunction, CompiledDensityFunctionImpl {

    @Shadow
    @Final
    private DensityFunction shiftX;
    @Shadow
    @Final
    private DensityFunction shiftY;
    @Shadow
    @Final
    private DensityFunction shiftZ;

    @Shadow
    @Final
    private @Nullable DensityFunction.class_7270 noise;
    @Shadow @Final private double xzScale;
    @Shadow @Final private double yScale;
    @Unique
    private long pointer = 0L;

    @Unique
    private String errorMessage = null;


    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (this.noise == null || this.noise.noise() == null) {
            this.pointer = NativeInterface.createDFIShiftedNoiseData(true, 0, 0, 0, 0, 0, 0, 0, 0);
            NativeMemoryTracker.registerAllocatedMemory(
                    this,
                    NativeInterface.SIZEOF_density_function_data + NativeInterface.SIZEOF_dfi_shifted_noise_data,
                    this.pointer
            );
        } else {
            if (!DensityFunctionUtils.isCompiled(this.shiftX, this.shiftY, this.shiftZ)) {
                if (DensityFunctionUtils.DEBUG) {
                    this.errorMessage = DensityFunctionUtils.getErrorMessage(
                            this,
                            ImmutableMap.of(
                                    "shiftX", this.shiftX,
                                    "shiftY", this.shiftY,
                                    "shiftZ", this.shiftZ
                            )
                    );
                    assert this.errorMessage != null;
                    System.err.println("Failed to compile density function: shifted_noise %s".formatted(this));
                    System.err.println(DensityFunctionUtils.indent(this.errorMessage, false));
                }
                return;
            }

            this.pointer = NativeInterface.createDFIShiftedNoiseData(
                    false,
                    ((CompiledDensityFunctionImpl) this.shiftX).getDFIPointer(),
                    ((CompiledDensityFunctionImpl) this.shiftY).getDFIPointer(),
                    ((CompiledDensityFunctionImpl) this.shiftZ).getDFIPointer(),
                    this.xzScale,
                    this.yScale,
                    ((NativeStruct) ((IDoublePerlinNoiseSampler) this.noise.noise()).getFirstSampler()).getNativePointer(),
                    ((NativeStruct) ((IDoublePerlinNoiseSampler) this.noise.noise()).getSecondSampler()).getNativePointer(),
                    ((IDoublePerlinNoiseSampler) this.noise.noise()).getAmplitude()
            );

            NativeMemoryTracker.registerAllocatedMemory(
                    this,
                    NativeInterface.SIZEOF_density_function_data + NativeInterface.SIZEOF_dfi_shifted_noise_data,
                    this.pointer
            );
        }

    }

    @Override
    public double sample(DensityFunction.NoisePos pos) {
        if (this.noise == null || this.noise.noise() == null) {
            return 0.0;
        } else if (this.pointer != 0L) {
            return NativeInterface.dfiBindingsSingleOp(this.pointer, pos.blockX(), pos.blockY(), pos.blockZ());
        } else {
            // TODO [VanillaCopy]
            double d = (double)pos.blockX() * this.xzScale + this.shiftX.sample(pos);
            double e = (double)pos.blockY() * this.yScale + this.shiftY.sample(pos);
            double f = (double)pos.blockZ() * this.xzScale + this.shiftZ.sample(pos);
            return this.noise.method_42356(d, e, f);
        }
    }

    @Override
    public void method_40470(double[] ds, DensityFunction.class_6911 arg) {
        if (this.noise == null || this.noise.noise() == null) {
            Arrays.fill(ds, 0.0);
        } else if (arg instanceof CompiledDensityFunctionArg dfa && dfa.getDFAPointer() != 0 && this.pointer != 0) {
            NativeInterface.dfiBindingsMultiOp(this.pointer, dfa.getDFAPointer(), ds);
        } else {
            arg.method_40478(ds, this);
        }
    }

    @Override
    public long getDFIPointer() {
        return this.pointer;
    }

    @Nullable
    @Override
    public String getCompilationFailedReason() {
        return this.errorMessage;
    }

}
