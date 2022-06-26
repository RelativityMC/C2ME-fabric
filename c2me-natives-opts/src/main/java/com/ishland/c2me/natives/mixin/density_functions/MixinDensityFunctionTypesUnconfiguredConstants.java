package com.ishland.c2me.natives.mixin.density_functions;

import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({
        DensityFunctionTypes.Beardifier.class,
        DensityFunctionTypes.BlendAlpha.class,
        DensityFunctionTypes.BlendOffset.class,
})
public abstract class MixinDensityFunctionTypesUnconfiguredConstants implements DensityFunction.Base, CompiledDensityFunctionImpl {

    @Unique
    private long pointer = 0L;

    @Unique
    private DensityFunctionTypes.Constant constant;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        constant = new DensityFunctionTypes.Constant(this.sample(null));
        this.pointer = ((CompiledDensityFunctionImpl) constant).getDFIPointer();
    }

    @Override
    public long getDFIPointer() {
        return this.pointer;
    }
}
