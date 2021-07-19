package com.ishland.c2me.mixin.access;

import net.minecraft.world.gen.StructureWeightSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructureWeightSampler.class)
public interface IStructureWeightSampler {

    @Invoker
    double invokeGetWeight(int x, int y, int z);

}
