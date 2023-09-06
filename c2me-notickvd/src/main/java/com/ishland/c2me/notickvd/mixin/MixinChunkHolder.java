package com.ishland.c2me.notickvd.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder {

    @Shadow public abstract CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> getAccessibleFuture();

    @Shadow @Nullable public abstract WorldChunk getWorldChunk();

    @Shadow public abstract ChunkPos getPos();

    @Shadow @Nullable public abstract WorldChunk getAccessibleChunk();

    @Redirect(method = {"markForBlockUpdate", "markForLightUpdate"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getWorldChunk()Lnet/minecraft/world/chunk/WorldChunk;"), require = 2)
    private WorldChunk redirectWorldChunk(ChunkHolder chunkHolder) {
        return this.getAccessibleChunk();
    }

}
