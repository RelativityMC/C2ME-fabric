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

@Mixin(DensityFunctionTypes.RangeChoice.class)
public abstract class MixinDensityFunctionTypesRangeChoice implements DensityFunction, CompiledDensityFunctionImpl {

    @Shadow
    @Final
    private DensityFunction input;
    @Shadow
    @Final
    private DensityFunction whenInRange;
    @Shadow
    @Final
    private DensityFunction whenOutOfRange;
    @Shadow
    @Final
    private double minInclusive;
    @Shadow
    @Final
    private double maxExclusive;
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
            DensityFunctionUtils.triggerCompilationIfNeeded(this.input, this.whenInRange, this.whenOutOfRange);
        }

        if (!DensityFunctionUtils.isCompiled(this.input, this.whenInRange, this.whenOutOfRange)) {
            if (DensityFunctionUtils.DEBUG) {
                this.errorMessage = DensityFunctionUtils.getErrorMessage(
                        this,
                        ImmutableMap.of(
                                "input", this.input,
                                "whenInRange", this.whenInRange,
                                "whenOutOfRange", this.whenOutOfRange
                        )
                );
                assert this.errorMessage != null;
                System.err.println("Failed to compile density function: range_choice %s".formatted(this));
                System.err.println(DensityFunctionUtils.indent(this.errorMessage, false));
            }
            return;
        }
        this.pointer = NativeInterface.createDFIRangeChoiceData(
                ((CompiledDensityFunctionImpl) this.input).getDFIPointer(),
                this.minInclusive,
                this.maxExclusive,
                ((CompiledDensityFunctionImpl) this.whenInRange).getDFIPointer(),
                ((CompiledDensityFunctionImpl) this.whenOutOfRange).getDFIPointer()
        );
        NativeMemoryTracker.registerAllocatedMemory(
                this,
                NativeInterface.SIZEOF_density_function_data + NativeInterface.SIZEOF_dfi_range_choice_data,
                this.pointer
        );
    }

    @Override
    public double sample(DensityFunction.NoisePos pos) {
        if (DensityFunctionUtils.isSafeForNative(pos) && this.pointer != 0) {
            return NativeInterface.dfiBindingsSingleOp(this.pointer, pos.blockX(), pos.blockY(), pos.blockZ());
        } else {
            // TODO [VanillaCopy]
            double d = this.input.sample(pos);
            return d >= this.minInclusive && d < this.maxExclusive ? this.whenInRange.sample(pos) : this.whenOutOfRange.sample(pos);
        }
    }

    @Override
    public void applyEach(double[] densities, EachApplier applier) {
        if (applier instanceof CompiledDensityFunctionArg dfa && dfa.getDFAPointer() != 0 && DensityFunctionUtils.isSafeForNative(applier) && this.pointer != 0) {
            NativeInterface.dfiBindingsMultiOp(this.pointer, dfa.getDFAPointer(), densities);
        } else {
            this.input.applyEach(densities, applier);

            for (int i = 0; i < densities.length; ++i) {
                double d = densities[i];
                if (d >= this.minInclusive && d < this.maxExclusive) {
                    densities[i] = this.whenInRange.sample(applier.getPosAt(i));
                } else {
                    densities[i] = this.whenOutOfRange.sample(applier.getPosAt(i));
                }
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
        final DensityFunction apply1 = this.whenInRange.apply(visitor);
        final DensityFunction apply2 = this.whenOutOfRange.apply(visitor);
        if (apply == this.input && apply1 == this.whenInRange && apply2 == this.whenOutOfRange)
            return visitor.apply(this);
        return visitor.apply(
                new DensityFunctionTypes.RangeChoice(
                        apply, this.minInclusive, this.maxExclusive, apply1, apply2
                )
        );
    }

}
