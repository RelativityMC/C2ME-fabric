package com.ishland.c2me.mixin.fixes.worldgen.threading;

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
