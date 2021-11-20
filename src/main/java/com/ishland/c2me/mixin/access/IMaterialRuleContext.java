package com.ishland.c2me.mixin.access;

import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MaterialRules.MaterialRuleContext.class)
public interface IMaterialRuleContext {

    @Invoker
    int invokeMethod_39551();

}
