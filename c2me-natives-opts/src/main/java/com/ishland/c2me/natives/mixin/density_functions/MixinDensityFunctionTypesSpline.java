package com.ishland.c2me.natives.mixin.density_functions;

import com.google.common.collect.ImmutableMap;
import com.ishland.c2me.natives.common.CompiledDensityFunctionArg;
import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import com.ishland.c2me.natives.common.CompiledSpline;
import com.ishland.c2me.natives.common.DensityFunctionUtils;
import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import net.minecraft.util.math.Spline;
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

@Mixin(DensityFunctionTypes.Spline.class)
public abstract class MixinDensityFunctionTypesSpline implements DensityFunction, CompiledDensityFunctionImpl {

    @Shadow @Final private Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> spline;
    @Unique
    private long pointer = 0L;

    @Unique
    private String errorMessage = null;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void onInit(CallbackInfo info) {
        if (!DensityFunctionUtils.isCompiled(this.spline)) {
            if (DensityFunctionUtils.DEBUG) {
                this.errorMessage = DensityFunctionUtils.getErrorMessage(
                        this,
                        ImmutableMap.of("spline", this.spline)
                );
                assert this.errorMessage != null;
                System.err.println("Failed to compile density function: clamp %s".formatted(this));
                System.err.println(DensityFunctionUtils.indent(this.errorMessage, false));
            }
            return;
        }
        this.pointer = NativeInterface.createDFISpline(((CompiledSpline) this.spline).getSplinePointer());
        NativeMemoryTracker.registerAllocatedMemory(
                this,
                NativeInterface.SIZEOF_density_function_data,
                this.pointer
        );
    }

    @Override
    public double sample(DensityFunction.NoisePos pos) {
        if (DensityFunctionUtils.isSafeForNative(pos) && this.pointer != 0) {
            return NativeInterface.dfiBindingsSingleOp(this.pointer, pos.blockX(), pos.blockY(), pos.blockZ());
        } else {
            // TODO [VanillaCopy]
            return this.spline.apply(new DensityFunctionTypes.Spline.SplinePos(pos));
        }
    }

    @Override
    public void applyEach(double[] ds, EachApplier arg) {
        if (arg instanceof CompiledDensityFunctionArg dfa && dfa.getDFAPointer() != 0 && DensityFunctionUtils.isSafeForNative(arg) && this.pointer != 0) {
            NativeInterface.dfiBindingsMultiOp(this.pointer, dfa.getDFAPointer(), ds);
        } else {
            // TODO [VanillaCopy]
            arg.applyEach(ds, this);
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
