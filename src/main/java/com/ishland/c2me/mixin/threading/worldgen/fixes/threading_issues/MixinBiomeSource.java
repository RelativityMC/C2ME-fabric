package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Map;

@Mixin(BiomeSource.class)
public class MixinBiomeSource {

    @Mutable
    @Shadow @Final protected Map<StructureFeature<?>, Boolean> structureFeatures;

    @Inject(method = "<init>(Ljava/util/List;)V", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.structureFeatures = Collections.synchronizedMap(structureFeatures);
    }

}
