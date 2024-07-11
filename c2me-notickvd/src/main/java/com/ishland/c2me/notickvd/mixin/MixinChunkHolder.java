package com.ishland.c2me.notickvd.mixin;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder {

    @Shadow public abstract CompletableFuture<OptionalChunk<WorldChunk>> getAccessibleFuture();

    @Redirect(method = {"markForBlockUpdate", "markForLightUpdate"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getWorldChunk()Lnet/minecraft/world/chunk/WorldChunk;"), require = 2)
    private WorldChunk redirectWorldChunk(ChunkHolder chunkHolder) {
        return this.getAccessibleFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).orElse(null);
    }

}
