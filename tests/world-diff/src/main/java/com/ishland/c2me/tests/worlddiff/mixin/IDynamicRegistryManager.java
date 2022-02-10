package com.ishland.c2me.tests.worlddiff.mixin;

import net.minecraft.util.registry.DynamicRegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DynamicRegistryManager.class)
public interface IDynamicRegistryManager {

    @Invoker
    static DynamicRegistryManager.class_6893 invokeMethod_40314() {
        throw new AbstractMethodError();
    }

}
