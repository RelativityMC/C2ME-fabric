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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DensityFunctionTypes.WeirdScaledSampler.class)
public abstract class MixinDensityFunctionTypesWeirdScaledSampler implements DensityFunctionTypes.Positional, CompiledDensityFunctionImpl {

    @Shadow @Final private DensityFunction input;
    @Shadow @Final private DensityFunctionTypes.WeirdScaledSampler.RarityValueMapper rarityValueMapper;
    @Shadow @Final private Noise noise;
    @Unique
    private long pointer = 0L;

    @Unique
    private String errorMessage = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        compileIfNeeded(false);
    }

    @Override
    public void compileIfNeeded(boolean includeParents) {
        if (this.pointer != 0L) return;

        if (includeParents) {
            DensityFunctionUtils.triggerCompilationIfNeeded(this.input);
        }

        if (!DensityFunctionUtils.isCompiled(this.input)) {
            if (DensityFunctionUtils.DEBUG) {
                this.errorMessage = DensityFunctionUtils.getErrorMessage(
                        this,
                        ImmutableMap.of("input", this.input)
                );
                assert this.errorMessage != null;
                System.err.println("Failed to compile density function: weird_scaled_sampler %s".formatted(this));
                System.err.println(DensityFunctionUtils.indent(this.errorMessage, false));
            }
            return;
        }
        if (this.noise == null || this.noise.noise() == null) {
            this.pointer = NativeInterface.createDFIWeirdScaledSampler(
                    ((CompiledDensityFunctionImpl) this.input).getDFIPointer(),
                    DensityFunctionUtils.mapWeirdScaledSamplerTypeToNative(this.rarityValueMapper),
                    true,
                    0,
                    0,
                    0
            );
        } else {
            this.pointer = NativeInterface.createDFIWeirdScaledSampler(
                    ((CompiledDensityFunctionImpl) this.input).getDFIPointer(),
                    DensityFunctionUtils.mapWeirdScaledSamplerTypeToNative(this.rarityValueMapper),
                    false,
                    ((NativeStruct) ((IDoublePerlinNoiseSampler) this.noise.noise()).getFirstSampler()).getNativePointer(),
                    ((NativeStruct) ((IDoublePerlinNoiseSampler) this.noise.noise()).getSecondSampler()).getNativePointer(),
                    ((IDoublePerlinNoiseSampler) this.noise.noise()).getAmplitude()
            );
        }
        NativeMemoryTracker.registerAllocatedMemory(
                this,
                NativeInterface.SIZEOF_density_function_data + NativeInterface.SIZEOF_dfi_weird_scaled_sampler,
                this.pointer
        );
    }

    @Override
    public double sample(DensityFunction.NoisePos pos) {
        if (DensityFunctionUtils.isSafeForNative(pos) && this.pointer != 0) {
            return NativeInterface.dfiBindingsSingleOp(this.pointer, pos.blockX(), pos.blockY(), pos.blockZ());
        } else {
            // TODO [VanillaCopy]
            return this.apply(pos, this.input().sample(pos));
        }
    }

    @Override
    public void fill(double[] ds, EachApplier arg) {
        if (arg instanceof CompiledDensityFunctionArg dfa && dfa.getDFAPointer() != 0 && DensityFunctionUtils.isSafeForNative(arg) && this.pointer != 0) {
            NativeInterface.dfiBindingsMultiOp(this.pointer, dfa.getDFAPointer(), ds);
        } else {
            // TODO [VanillaCopy]
            this.input().fill(ds, arg);

            for(int i = 0; i < ds.length; ++i) {
                ds[i] = this.apply(arg.at(i), ds[i]);
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
        final DensityFunction apply = this.input.apply(visitor);
        final Noise apply1 = visitor.apply(this.noise);
        if (apply == this.input && apply1 == this.noise) return visitor.apply(this);
        return visitor.apply(new DensityFunctionTypes.WeirdScaledSampler(apply, apply1, this.rarityValueMapper));
    }

}
