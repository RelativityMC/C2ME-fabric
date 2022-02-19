package com.ishland.c2me.mixin.fixes.worldgen.threading;

import com.ishland.c2me.common.GlobalExecutors;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {

    @Shadow
    private ChunkGenerator chunkGenerator;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        GlobalExecutors.executor.execute(() -> this.chunkGenerator.getConcentricRingsStartChunks(null));
    }

}
