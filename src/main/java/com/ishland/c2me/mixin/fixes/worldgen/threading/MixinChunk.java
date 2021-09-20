package com.ishland.c2me.mixin.fixes.worldgen.threading;

import com.ishland.c2me.common.util.CMETrackingMap;
import net.minecraft.structure.StructureStart;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Chunk.class)
public abstract class MixinChunk {

    @Mutable
    @Shadow @Final private Map<StructureFeature<?>, StructureStart<?>> field_34552;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.field_34552 = new CMETrackingMap<>(this.field_34552);
    }

}
