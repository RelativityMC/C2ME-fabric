package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading.checks;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.Chunk;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {

    @Shadow
    @Final
    private LongSet loadedChunks;

    @Shadow @Final private static Logger LOGGER;

    @Dynamic
    @Inject(method = "method_17227", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;loadEntities()V"), cancellable = false)
    // lambda expression in convertToFullChunk
    private void afterLoadToWorld(ChunkHolder chunkHolder, Chunk protoChunk, CallbackInfoReturnable<CompletableFuture<OptionalChunk<Chunk>>> cir) {
        if (this.loadedChunks.contains(chunkHolder.getPos().toLong()))
            LOGGER.error("Double scheduling chunk loading detected on chunk {}", chunkHolder.getPos());
    }

}
