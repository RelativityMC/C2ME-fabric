package com.ishland.c2me.mixin.optimization.reduce_allocs.lazy_material_rules;

import com.ishland.c2me.common.optimization.reduce_allocs.LazyMaterialRuleContext;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MaterialRules.MaterialRuleContext.class)
public abstract class MixinMaterialRuleContext implements LazyMaterialRuleContext {

    @Shadow
    protected abstract void initVerticalContext(int i, int j, int k, int l, int m, int n);

    @Shadow private long uniquePosValue;
    @Unique
    private boolean hasPendingLazyInit = false;
    @Unique
    private int lazyArg0;
    @Unique
    private int lazyArg1;
    @Unique
    private int lazyArg2;
    @Unique
    private int lazyArg3;
    @Unique
    private int lazyArg4;
    @Unique
    private int lazyArg5;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.hasPendingLazyInit = false;
    }

    @Override
    public void lazyInitVerticalContext(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        this.uniquePosValue ++;
        this.lazyArg0 = arg0;
        this.lazyArg1 = arg1;
        this.lazyArg2 = arg2;
        this.lazyArg3 = arg3;
        this.lazyArg4 = arg4;
        this.lazyArg5 = arg5;
        this.hasPendingLazyInit = true;
    }

    @Override
    public void doInitVerticalContext() {
        if (hasPendingLazyInit) {
            this.hasPendingLazyInit = false;
            this.uniquePosValue --;
            this.initVerticalContext(
                    this.lazyArg0,
                    this.lazyArg1,
                    this.lazyArg2,
                    this.lazyArg3,
                    this.lazyArg4,
                    this.lazyArg5
            );
        }
    }
}
