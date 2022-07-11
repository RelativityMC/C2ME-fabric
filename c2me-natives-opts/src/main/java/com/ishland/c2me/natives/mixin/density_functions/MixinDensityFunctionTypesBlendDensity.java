package com.ishland.c2me.natives.mixin.density_functions;

import com.google.common.collect.ImmutableMap;
import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import com.ishland.c2me.natives.common.DensityFunctionUtils;
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

@Mixin(DensityFunctionTypes.BlendDensity.class)
public abstract class MixinDensityFunctionTypesBlendDensity implements DensityFunctionTypes.Positional, CompiledDensityFunctionImpl {

    @Shadow @Final private DensityFunction input;
    @Unique
    private long pointer = 0L;

    @Unique
    private String errorMessage = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (!DensityFunctionUtils.isCompiled(this.input)) {
            if (DensityFunctionUtils.DEBUG) {
                this.errorMessage = DensityFunctionUtils.getErrorMessage(
                        this,
                        ImmutableMap.of("input", this.input)
                );
                assert this.errorMessage != null;
                System.err.println("Failed to pass-through density function: blend_density %s".formatted(this));
                System.err.println(DensityFunctionUtils.indent(this.errorMessage, false));
            }
            return;
        }

        this.pointer = ((CompiledDensityFunctionImpl) this.input).getDFIPointer();
        // memory management not needed
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

    @Override
    public CompiledDensityFunctionImpl.Type getDFIType() {
        return CompiledDensityFunctionImpl.Type.PASS_THROUGH;
    }

    /**
     * @author ishland
     * @reason reduce allocs
     */
    @Overwrite
    public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        final DensityFunction apply = this.input.apply(visitor);
        if (apply == this.input) return this;
        return visitor.apply(new DensityFunctionTypes.BlendDensity(apply));
    }

}
