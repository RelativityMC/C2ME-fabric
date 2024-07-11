package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.ishland.c2me.rewrites.chunksystem.common.NewChunkHolderVanillaInterface;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkHolder.class)
public class MixinChunkHolder {

    @Shadow private volatile CompletableFuture<OptionalChunk<WorldChunk>> accessibleFuture;

    @Shadow private volatile CompletableFuture<OptionalChunk<WorldChunk>> tickingFuture;

    @Shadow private volatile CompletableFuture<OptionalChunk<WorldChunk>> entityTickingFuture;

    @Shadow private CompletableFuture<?> levelIncreaseFuture;

    @Shadow private CompletableFuture<?> savingFuture;

    @WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;setLevel(I)V"))
    private boolean noopSetLevel(ChunkHolder instance, int level) {
        //noinspection ConstantValue
        return !((Object) this instanceof NewChunkHolderVanillaInterface);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void failFastIncompatibility(CallbackInfo ci) {
        //noinspection ConstantValue
        if (((Object) this instanceof NewChunkHolderVanillaInterface)) {
            this.accessibleFuture = null;
            this.tickingFuture = null;
            this.entityTickingFuture = null;
            this.levelIncreaseFuture = null;
            this.savingFuture = null;
        }
    }

}
