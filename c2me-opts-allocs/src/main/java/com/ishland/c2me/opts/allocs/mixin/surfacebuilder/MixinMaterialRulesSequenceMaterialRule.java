package com.ishland.c2me.opts.allocs.mixin.surfacebuilder;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MaterialRules.SequenceMaterialRule.class)
public class MixinMaterialRulesSequenceMaterialRule {

    private static final MaterialRules.BlockStateRule EMPTY = new MaterialRules.SequenceBlockStateRule(List.of());

    @Shadow
    @Final
    private List<MaterialRules.MaterialRule> sequence;

    @Unique
    private MaterialRules.MaterialRule[] sequenceArray;

    @Unique
    private boolean isSingleOrNoElement;

    @Unique
    private MaterialRules.MaterialRule firstElement;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.sequenceArray = this.sequence.toArray(MaterialRules.MaterialRule[]::new);
        this.isSingleOrNoElement = this.sequenceArray.length <= 1;
        this.firstElement = this.sequenceArray.length == 0 ? null : this.sequenceArray[0];
    }

    public MaterialRules.BlockStateRule apply(MaterialRules.MaterialRuleContext materialRuleContext) {
        if (this.isSingleOrNoElement) {
            return this.firstElement != null ? this.firstElement.apply(materialRuleContext) : EMPTY;
        } else {
            @SuppressWarnings("UnstableApiUsage")
            ImmutableList.Builder<MaterialRules.BlockStateRule> builder = ImmutableList.builderWithExpectedSize(this.sequenceArray.length);

            for (MaterialRules.MaterialRule materialRule : this.sequenceArray) {
                builder.add(materialRule.apply(materialRuleContext));
            }

            return new MaterialRules.SequenceBlockStateRule(builder.build());
        }
    }

}
