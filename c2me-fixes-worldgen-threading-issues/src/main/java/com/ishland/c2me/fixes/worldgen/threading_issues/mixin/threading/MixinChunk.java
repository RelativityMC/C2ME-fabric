package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Chunk.class)
public abstract class MixinChunk {

//    @Mutable
//    @Shadow
//    @Final
//    private Map<StructureFeature<?>, StructureStart<?>> structureStarts;
//
//    @Inject(method = "<init>", at = @At("RETURN"))
//    private void onInit(CallbackInfo info) {
//        this.structureStarts = new CMETrackingMap<>(this.structureStarts);
//    }

}
