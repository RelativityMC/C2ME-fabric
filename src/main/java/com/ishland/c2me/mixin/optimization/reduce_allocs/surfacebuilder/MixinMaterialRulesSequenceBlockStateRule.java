package com.ishland.c2me.mixin.optimization.reduce_allocs.surfacebuilder;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MaterialRules.SequenceBlockStateRule.class)
public class MixinMaterialRulesSequenceBlockStateRule {

    @Shadow @Final private List<MaterialRules.BlockStateRule> rules;
    @Unique
    private MaterialRules.BlockStateRule[] rulesArray;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.rulesArray = this.rules.toArray(MaterialRules.BlockStateRule[]::new);
    }

    /**
     * @author ishland
     * @reason use array for iteration
     */
    @Overwrite
    public BlockState tryApply(int i, int j, int k) {
        // TODO [VanillaCopy]
        for(MaterialRules.BlockStateRule blockStateRule : this.rulesArray) {
            BlockState blockState = blockStateRule.tryApply(i, j, k);
            if (blockState != null) {
                return blockState;
            }
        }

        return null;
    }

}
