package com.ishland.c2me.natives.mixin.density_functions;

import com.google.common.collect.ImmutableMap;
import com.ishland.c2me.natives.common.CompiledDensityFunctionArg;
import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import com.ishland.c2me.natives.common.DensityFunctionUtils;
import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
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

@Mixin(DensityFunctionTypes.LinearOperation.class)
public abstract class MixinDensityFunctionTypesHalfOperation implements DensityFunctionTypes.Unary, CompiledDensityFunctionImpl {

    @Shadow
    @Final
    private DensityFunction input;

    @Shadow public abstract DensityFunctionTypes.BinaryOperationLike.Type type();

    @Shadow @Final private double argument;
    @Shadow @Final private DensityFunctionTypes.LinearOperation.SpecificType specificType;
    @Unique
    private long pointer;

    @Unique
    private String errorMessage = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        if (!DensityFunctionUtils.isCompiled(this.input)) {
            if (DensityFunctionUtils.DEBUG) {
                this.errorMessage = DensityFunctionUtils.getErrorMessage(
                        this,
                        ImmutableMap.of("input", this.input)
                );
                assert this.errorMessage != null;
                System.err.println("Failed to compile density function: operation_half %s".formatted(this));
                System.err.println(DensityFunctionUtils.indent(this.errorMessage, false));
            }
            return;
        }

        this.pointer = NativeInterface.createDFIOperationHalf(
                DensityFunctionUtils.mapOperationToNative(this.type()),
                ((CompiledDensityFunctionImpl) this.input).getDFIPointer(),
                this.argument
        );
        NativeMemoryTracker.registerAllocatedMemory(
                this,
                NativeInterface.SIZEOF_density_function_data + NativeInterface.SIZEOF_dfi_operation_half_data,
                this.pointer
        );
    }

    @Override
    public double sample(DensityFunction.NoisePos pos) {
        if (DensityFunctionUtils.isSafeForNative(pos) && this.pointer != 0) {
            return NativeInterface.dfiBindingsSingleOp(this.pointer, pos.blockX(), pos.blockY(), pos.blockZ());
        } else {
            // [VanillaCopy]
            return this.apply(this.input().sample(pos));
        }
    }

    @Override
    public void applyEach(double[] ds, EachApplier arg) {
        if (arg instanceof CompiledDensityFunctionArg dfa && dfa.getDFAPointer() != 0 && DensityFunctionUtils.isSafeForNative(arg) && this.pointer != 0) {
            NativeInterface.dfiBindingsMultiOp(this.pointer, dfa.getDFAPointer(), ds);
        } else {
            // [VanillaCopy]
            this.input().applyEach(ds, arg);

            for(int i = 0; i < ds.length; ++i) {
                ds[i] = this.apply(ds[i]);
            }
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

    /**
     * @author ishland
     * @reason reduce allocs
     */
    @Overwrite
    public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        DensityFunction densityFunction = this.input.apply(visitor);
        if (densityFunction == this.input) return this;
        double d = densityFunction.minValue();
        double e = densityFunction.maxValue();
        double f;
        double g;
        if (this.specificType == DensityFunctionTypes.LinearOperation.SpecificType.ADD) {
            f = d + this.argument;
            g = e + this.argument;
        } else if (this.argument >= 0.0) {
            f = d * this.argument;
            g = e * this.argument;
        } else {
            f = e * this.argument;
            g = d * this.argument;
        }

        return new DensityFunctionTypes.LinearOperation(this.specificType, densityFunction, f, g, this.argument);
    }

}
