package com.ishland.c2me.mixin.fixes.worldgen.threading;

import com.ishland.c2me.common.fixes.worldgen.threading.ConcurrentFlagMatrix;
import net.minecraft.structure.WoodlandMansionGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings({"InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
@Mixin(WoodlandMansionGenerator.MansionParameters.class)
public class MixinWoodlandMansionGeneratorMansionParameters {

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/structure/WoodlandMansionGenerator$FlagMatrix"))
    private WoodlandMansionGenerator.FlagMatrix redirectNewMatrix(int n, int m, int fallback) {
        return new ConcurrentFlagMatrix(n, m, fallback);
    }

}
