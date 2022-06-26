package com.ishland.c2me.natives.mixin.density_functions;

import com.ishland.c2me.natives.common.CompiledDensityFunctionArg;
import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import com.ishland.c2me.natives.common.NativeStruct;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DensityFunctionTypes.EndIslands.class)
public abstract class MixinDensityFunctionTypesEndIslands implements DensityFunction.Base, CompiledDensityFunctionImpl {

    @Shadow
    @Final
    private SimplexNoiseSampler sampler;
    @Unique
    private long pointer = 0;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
//        System.out.println("Compiling density function: end_islands %s".formatted(this));
        this.pointer = NativeInterface.createDFIEndIslands(((NativeStruct) this.sampler).getNativePointer());
        NativeMemoryTracker.registerAllocatedMemory(this, NativeInterface.SIZEOF_density_function_data + NativeInterface.SIZEOF_dfi_end_islands_data, this.pointer);
    }

    /**
     * @author ishland
     * @reason use native method
     */
    @Overwrite
    public double sample(DensityFunction.NoisePos pos) {
        return NativeInterface.dfiBindingsSingleOp(this.pointer, pos.blockX(), pos.blockY(), pos.blockZ());
    }

    @Override
    public void applyEach(double[] ds, EachApplier arg) {
        if (arg instanceof CompiledDensityFunctionArg dfa && dfa.getDFAPointer() != 0) {
            NativeInterface.dfiBindingsMultiOp(this.pointer, dfa.getDFAPointer(), ds);
        } else {
            Base.super.applyEach(ds, arg);
        }
    }

    @Override
    public long getDFIPointer() {
        return this.pointer;
    }

    @Override
    public String toString() {
        return "EndIslands{" +
                "noise=" + sampler +
                '}';
    }
}
