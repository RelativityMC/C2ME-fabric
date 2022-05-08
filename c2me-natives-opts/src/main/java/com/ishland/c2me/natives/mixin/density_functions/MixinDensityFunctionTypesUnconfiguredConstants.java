package com.ishland.c2me.natives.mixin.density_functions;

import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({
        DensityFunctionTypes.Beardifier.class,
        DensityFunctionTypes.BlendAlpha.class,
        DensityFunctionTypes.BlendOffset.class,
})
public abstract class MixinDensityFunctionTypesUnconfiguredConstants implements DensityFunction.class_6913, CompiledDensityFunctionImpl {

    private long pointer = 0L;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.pointer = ((CompiledDensityFunctionImpl) new DensityFunctionTypes.Constant(this.sample(null))).getDFIPointer();
    }

    @Override
    public long getDFIPointer() {
        return this.pointer;
    }
}
