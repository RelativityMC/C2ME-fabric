package com.ishland.c2me.mixin.notickvd;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkManager.class)
public class MixinServerChunkManager {

    @Dynamic
    @Redirect(method = "method_37411", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getTickingFuture()Ljava/util/concurrent/CompletableFuture;")) // TODO lambda expression in tickChunks after "broadcast"
    private static CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> redirectTickingFuture(ChunkHolder chunkHolder) {
        return chunkHolder.getAccessibleFuture();
    }

}
