package com.ishland.c2me.mixin.optimization.reduce_allocs.lazy_material_rules;

import com.ishland.c2me.common.optimization.reduce_allocs.LazyMaterialRuleContext;
import com.ishland.c2me.mixin.access.IMaterialRuleContext;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SurfaceBuilder.class)
public class MixinSurfaceBuilder {

    @Redirect(method = "buildSurface", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/surfacebuilder/MaterialRules$MaterialRuleContext;initVerticalContext(IIIIII)V"))
    private void redirectInitVerticalContext(MaterialRules.MaterialRuleContext instance, int i, int j, int k, int l, int m, int n) {
        ((LazyMaterialRuleContext) instance).lazyInitVerticalContext(i, j, k, l, m, n);
    }

    @Redirect(method = "buildSurface", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/surfacebuilder/MaterialRules$MaterialRuleContext;method_39551()I"))
    private int onContextUse(MaterialRules.MaterialRuleContext instance) {
        ((LazyMaterialRuleContext) instance).doInitVerticalContext();
        return ((IMaterialRuleContext) instance).invokeMethod_39551();
    }

}
