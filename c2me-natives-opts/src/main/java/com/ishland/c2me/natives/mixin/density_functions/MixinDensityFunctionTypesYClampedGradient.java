package com.ishland.c2me.natives.mixin.density_functions;

import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DensityFunctionTypes.YClampedGradient.class)
public abstract class MixinDensityFunctionTypesYClampedGradient implements DensityFunction.Base, CompiledDensityFunctionImpl {

    @Shadow @Final private int fromY;
    @Shadow @Final private int toY;
    @Shadow @Final private double fromValue;
    @Shadow @Final private double toValue;
    @Unique
    private long pointer = 0;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
//        System.out.println("Compiling density function: y_clamped_gradient %s".formatted(this));
        this.pointer = NativeInterface.createDFIClampedGradientData(this.fromY, this.toY, this.fromValue, this.toValue);
        NativeMemoryTracker.registerAllocatedMemory(this, NativeInterface.SIZEOF_density_function_data + NativeInterface.SIZEOF_dfi_y_clamped_gradient_data, this.pointer);
    }

    @Override
    public void compileIfNeeded(boolean includeParents) {
        // no-op: always compilable
    }

//    /**
//     * @author ishland
//     * @reason use native method
//     */
//    @Overwrite
//    public double sample(DensityFunction.NoisePos pos) {
//        return NativeInterface.dfiBindingsSingleOp(this.pointer, pos.blockX(), pos.blockY(), pos.blockZ());
//    }
//
//    @Override
//    public void applyEach(double[] ds, DensityFunction.EachApplier arg) {
//        if (arg instanceof CompiledDensityFunctionArg dfa && dfa.getDFAPointer() != 0) {
//            NativeInterface.dfiBindingsMultiOp(this.pointer, dfa.getDFAPointer(), ds);
//        } else {
//            DensityFunction.Base.super.applyEach(ds, arg);
//        }
//    }

    @Override
    public long getDFIPointer() {
        return this.pointer;
    }

}
