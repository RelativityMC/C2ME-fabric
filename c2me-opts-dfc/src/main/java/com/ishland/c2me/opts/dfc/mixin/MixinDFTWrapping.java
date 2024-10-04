package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ducks.IFastCacheLike;
import com.ishland.c2me.opts.dfc.common.ducks.IEqualityOverriding;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DensityFunctionTypes.Wrapping.class)
public abstract class MixinDFTWrapping implements IFastCacheLike, IEqualityOverriding {

    @Mutable
    @Shadow @Final private DensityFunction wrapped;

    @Shadow public abstract DensityFunctionTypes.Wrapping.Type type();

    @Unique
    private Object c2me$optionalEquality;

    @Override
    public double c2me$getCached(int x, int y, int z, EvalType evalType) {
        return Double.longBitsToDouble(CACHE_MISS_NAN_BITS);
    }

    @Override
    public boolean c2me$getCached(double[] res, int[] x, int[] y, int[] z, EvalType evalType) {
        return false;
    }

    @Override
    public void c2me$cache(int x, int y, int z, EvalType evalType, double cached) {
        // nop
    }

    @Override
    public void c2me$cache(double[] res, int[] x, int[] y, int[] z, EvalType evalType) {
        // nop
    }

    @Override
    public DensityFunction c2me$getDelegate() {
        return this.wrapped;
    }

    @Override
    public DensityFunction c2me$withDelegate(DensityFunction delegate) {
        DensityFunctionTypes.Wrapping wrapping = new DensityFunctionTypes.Wrapping(this.type(), delegate);
        ((IEqualityOverriding) (Object) wrapping).c2me$overrideEquality(this);
        return wrapping;
    }

    @Override
    public void c2me$overrideEquality(Object object) {
        this.c2me$optionalEquality = object;
    }

    @WrapMethod(method = "hashCode")
    private int wrapHashCode(Operation<Integer> original) {
        Object c2me$optionalEquality1 = this.c2me$optionalEquality;
        if (c2me$optionalEquality1 != null) {
            return c2me$optionalEquality1.hashCode();
        } else {
            return original.call();
        }
    }

    @WrapMethod(method = "equals")
    private boolean wrapEquals(Object object, Operation<Boolean> original) {
        Object c2me$optionalEquality1 = this.c2me$optionalEquality;
        if (c2me$optionalEquality1 != null) {
            return c2me$optionalEquality1.equals(object);
        } else {
            return original.call(object);
        }
    }
}
