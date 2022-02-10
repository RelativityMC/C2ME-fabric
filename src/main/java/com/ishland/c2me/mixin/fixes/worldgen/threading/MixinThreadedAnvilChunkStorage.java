package com.ishland.c2me.mixin.fixes.worldgen.threading;

import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {

//    @Shadow private ChunkGenerator chunkGenerator;
//
//    @Inject(method = "<init>", at = @At("RETURN"))
//    private void onInit(CallbackInfo info) {
//        GlobalExecutors.executor.execute(() -> this.chunkGenerator.isStrongholdStartingChunk(new ChunkPos(0, 0)));
//    }

}
