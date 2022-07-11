package com.ishland.c2me.natives.mixin.density_functions;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DensityFunctionTypes.Wrapper.class)
public interface MixinDensityFunctionTypesWrapper extends DensityFunction {

    @Shadow DensityFunctionTypes.Wrapping.Type type();

    @Shadow DensityFunction wrapped();

    /**
     * @author ishland
     * @reason reduce allocs
     */
    @Overwrite
    default DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        final DensityFunction apply = this.wrapped().apply(visitor);
        if (apply == this.wrapped()) return visitor.apply(this);
        return visitor.apply(new DensityFunctionTypes.Wrapping(this.type(), apply));
    }

}
