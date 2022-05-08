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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DensityFunctionTypes.class_6925.class)
public abstract class MixinDensityFunctionTypesSingleOperation implements DensityFunctionTypes.class_6932, CompiledDensityFunctionImpl {

    @Shadow @Final private DensityFunction input;

    @Shadow public abstract DensityFunctionTypes.class_6925.Type type();

    @Shadow
    protected static double method_40521(DensityFunctionTypes.class_6925.Type type, double d) {
        throw new AbstractMethodError();
    }

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
        if (this.pointer != 0) {
            return NativeInterface.dfiBindingsSingleOp(this.pointer, pos.blockX(), pos.blockY(), pos.blockZ());
        } else {
            return method_40521(this.type(), this.input.sample(pos));
        }
    }

    @Override
    public void method_40470(double[] ds, class_6911 arg) {
        if (arg instanceof CompiledDensityFunctionArg dfa && dfa.getDFAPointer() != 0 && this.pointer != 0) {
            NativeInterface.dfiBindingsMultiOp(this.pointer, dfa.getDFAPointer(), ds);
        } else {
            // TODO [VanillaCopy]
            this.input.method_40470(ds, arg);
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

}
