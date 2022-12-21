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

@Mixin(DensityFunctionTypes.UnaryOperation.class)
public abstract class MixinDensityFunctionTypesUnaryOperation implements DensityFunctionTypes.Unary, CompiledDensityFunctionImpl {

    @Shadow
    @Final
    private DensityFunction input;

    @Shadow
    public abstract DensityFunctionTypes.UnaryOperation.Type type();

    @Shadow
    protected static double apply(DensityFunctionTypes.UnaryOperation.Type type, double d) {
        throw new AbstractMethodError();
    }

    @Shadow
    @Final
    private DensityFunctionTypes.UnaryOperation.Type type;

    @Shadow
    public static DensityFunctionTypes.UnaryOperation create(DensityFunctionTypes.UnaryOperation.Type type, DensityFunction input) {
        throw new AbstractMethodError();
    }

    @Unique
    private long pointer;

    @Unique
    private String errorMessage = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        compileIfNeeded(false);
    }

    @Override
    public void compileIfNeeded(boolean includeParents) {
        if (pointer != 0L) return;

        if (includeParents) {
            DensityFunctionUtils.triggerCompilationIfNeeded(this.input);
        }

        if (!DensityFunctionUtils.isCompiled(this.input)) {
            if (DensityFunctionUtils.DEBUG) {
                this.errorMessage = DensityFunctionUtils.getErrorMessage(
                        this,
                        ImmutableMap.of(
                                "input", this.input
                        )
                );
                assert this.errorMessage != null;
                System.err.println("Failed to compile density function: single_operation %s".formatted(this));
                System.err.println(DensityFunctionUtils.indent(this.errorMessage, false));
            }
            return;
        }

        this.pointer = NativeInterface.createDFISingleOperation(
                DensityFunctionUtils.mapSingleOperationToNative(this.type()),
                ((CompiledDensityFunctionImpl) this.input).getDFIPointer()
        );
        NativeMemoryTracker.registerAllocatedMemory(
                this,
                NativeInterface.SIZEOF_density_function_data + NativeInterface.SIZEOF_dfi_single_operation_data,
                this.pointer
        );
    }

    @Override
    public double sample(NoisePos pos) {
        if (DensityFunctionUtils.isSafeForNative(pos) && this.pointer != 0) {
            return NativeInterface.dfiBindingsSingleOp(this.pointer, pos.blockX(), pos.blockY(), pos.blockZ());
        } else {
            return apply(this.type(), this.input.sample(pos));
        }
    }

    @Override
    public void applyEach(double[] ds, EachApplier arg) {
        if (arg instanceof CompiledDensityFunctionArg dfa && dfa.getDFAPointer() != 0 && DensityFunctionUtils.isSafeForNative(arg) && this.pointer != 0) {
            NativeInterface.dfiBindingsMultiOp(this.pointer, dfa.getDFAPointer(), ds);
        } else {
            // TODO [VanillaCopy]
            this.input.applyEach(ds, arg);
            for (int i = 0; i < ds.length; ++i) {
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
    public DensityFunctionTypes.UnaryOperation apply(DensityFunction.DensityFunctionVisitor densityFunctionVisitor) {
        final DensityFunction apply = this.input.apply(densityFunctionVisitor);
        if (apply == this.input) return (DensityFunctionTypes.UnaryOperation) (Object) this;
        return create(this.type, apply);
    }

}
