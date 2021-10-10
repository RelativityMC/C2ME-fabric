package com.ishland.c2me.mixin.notickvd;

import com.ishland.c2me.common.notickvd.IChunkHolder;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder implements IChunkHolder {

    @Shadow public abstract CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> getAccessibleFuture();

    @Shadow @Nullable public abstract WorldChunk getWorldChunk();

    @Shadow public abstract ChunkPos getPos();

    @Unique
    @Override
    public WorldChunk getAccessibleChunk() {
        final Either<WorldChunk, ChunkHolder.Unloaded> either = this.getAccessibleFuture().getNow(null);
        return either == null ? null : either.left().orElseGet(this::getWorldChunk);
    }

    @Redirect(method = {"markForBlockUpdate", "markForLightUpdate"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getWorldChunk()Lnet/minecraft/world/chunk/WorldChunk;"), require = 2)
    private WorldChunk redirectWorldChunk(ChunkHolder chunkHolder) {
        return this.getAccessibleChunk();
    }

    /**
     * move tick scheduler things to ticking
     * private static synthetic method_20457(Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;Lcom/mojang/datafixers/util/Either;)Lcom/mojang/datafixers/util/Either;
     * @author ishland
     * @reason check above
     */
    @SuppressWarnings("OverwriteTarget")
    @Dynamic
    @Overwrite
    private static Either<WorldChunk, ChunkHolder.Unloaded> method_20457(ThreadedAnvilChunkStorage threadedAnvilChunkStorage, Either<WorldChunk, ChunkHolder.Unloaded> either) { // TODO lambda expression in tick at this.combineSavingFuture(<this>, "unfull")
        return either; // no-op
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;complete(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.BEFORE)) // TODO check ordinal when updating minecraft version
    private void redirectSetTickingFuture(ThreadedAnvilChunkStorage chunkStorage, CallbackInfo ci) {
        final WorldChunk accessibleChunk = getAccessibleChunk();
        if (accessibleChunk != null) {
            chunkStorage.enableTickSchedulers(accessibleChunk);
        } else {
//            System.err.println("Unable to set tick scheduler for chunk " + this.getPos());
            // Vanilla ignores this so we can also ignore this
        }
    }

}
