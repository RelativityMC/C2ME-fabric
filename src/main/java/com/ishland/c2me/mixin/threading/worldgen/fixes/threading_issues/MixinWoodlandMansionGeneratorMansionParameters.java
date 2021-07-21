package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes.ConcurrentFlagMatrix;
import net.minecraft.structure.WoodlandMansionGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WoodlandMansionGenerator.MansionParameters.class)
public class MixinWoodlandMansionGeneratorMansionParameters {

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/structure/WoodlandMansionGenerator$FlagMatrix"))
    private WoodlandMansionGenerator.FlagMatrix redirectNewMatrix(int n, int m, int fallback) {
        return new ConcurrentFlagMatrix(n, m, fallback);
    }

}
