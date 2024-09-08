package com.ishland.c2me.base.mixin.access;

import net.minecraft.world.gen.densityfunction.DensityFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DensityFunctions.CaveScaler.class)
public interface IDensityFunctionsCaveScaler {

    @Invoker
    public static double invokeScaleCaves(double value) {
        throw new AbstractMethodError();
    }

    @Invoker
    public static double invokeScaleTunnels(double value) {
        throw new AbstractMethodError();
    }

}
