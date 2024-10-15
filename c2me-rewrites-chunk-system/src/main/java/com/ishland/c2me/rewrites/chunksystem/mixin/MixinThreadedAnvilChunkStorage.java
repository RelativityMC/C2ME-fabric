package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ishland.c2me.base.common.scheduler.IVanillaChunkManager;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.ducks.IChunkSystemAccess;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkHolderVanillaInterface;
import com.ishland.c2me.rewrites.chunksystem.common.TheChunkSystem;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;

@Mixin(ServerChunkLoadingManager.class)
public class MixinThreadedAnvilChunkStorage implements IChunkSystemAccess {

    @Shadow @Final private ServerWorld world;
    private TheChunkSystem newSystem;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        newSystem = new TheChunkSystem(
                new ThreadFactoryBuilder()
                        .setNameFormat("chunksystem-" + this.world.getRegistryKey().getValue().toUnderscoreSeparatedString())
                        .build(),
                (ServerChunkLoadingManager) (Object) this
        );
    }

    /**
     * @author ishland
     * @reason replace chunk system
     */
    @Overwrite
    @Nullable
    public ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int i) {
        return this.newSystem.vanillaIf$setLevel(pos, level);
    }

    /**
     * @author ishland
     * @reason replace chunk system
     */
    @Overwrite
    @Nullable
    public ChunkHolder getCurrentChunkHolder(long pos) {
        final ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder = this.newSystem.getHolder(new ChunkPos(pos));
        if (holder != null) {
            synchronized (holder) {
                if (!holder.isOpen()) {
                    return null;
                } else {
                    return holder.getUserData().get();
                }
            }
        } else {
            return null;
        }
    }
    
    @ModifyArg(method = "save(Lnet/minecraft/world/chunk/Chunk;)Z", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"), require = 0)
    private Executor redirectSavingExecutor(Executor executor, @Local(argsOnly = true) Chunk chunk) {
        return ((IVanillaChunkManager) this).c2me$getSchedulingManager().positionedExecutor(chunk.getPos().toLong());
    }

    /**
     * @author ishland
     * @reason replace chunk system
     */
    @Overwrite
    @Nullable
    public ChunkHolder getChunkHolder(long pos) {
        return this.getCurrentChunkHolder(pos);
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/poi/PointOfInterestStorage;close()V", shift = At.Shift.AFTER))
    private void closeNewSystem(CallbackInfo ci) {
        this.newSystem.shutdown();
    }

    @Redirect(method = "save(Lnet/minecraft/world/chunk/Chunk;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkStatus;getChunkType()Lnet/minecraft/world/chunk/ChunkType;"), require = 0)
    private ChunkType alwaysSaveChunk(ChunkStatus instance) {
        return ChunkType.LEVELCHUNK;
    }

    @ModifyReturnValue(method = "shouldDelayShutdown", at = @At("RETURN"))
    private boolean delayShutdown(boolean original) {
        return original || this.newSystem.itemCount() != 0;
    }

    @Override
    public TheChunkSystem c2me$getTheChunkSystem() {
        return this.newSystem;
    }
}
