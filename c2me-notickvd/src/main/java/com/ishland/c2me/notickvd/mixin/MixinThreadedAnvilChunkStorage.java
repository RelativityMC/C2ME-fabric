package com.ishland.c2me.notickvd.mixin;

import com.ishland.c2me.base.mixin.access.IServerChunkManager;
import com.ishland.c2me.notickvd.common.Config;
import com.ishland.c2me.notickvd.common.IChunkTicketManager;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.PlayerChunkWatchingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage {

    @Shadow public abstract List<ServerPlayerEntity> getPlayersWatchingChunk(ChunkPos chunkPos, boolean onlyOnWatchDistanceEdge);

    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

    @Shadow @Final private PlayerChunkWatchingManager playerChunkWatchingManager;

    @Shadow protected abstract void sendToPlayers(WorldChunk chunk);

    @ModifyArg(method = "setViewDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I"), index = 2)
    private int modifyMaxVD(int max) {
        return 251;
    }

    @Redirect(method = "getPostProcessedChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getPostProcessedChunk()Lnet/minecraft/world/chunk/WorldChunk;"))
    private WorldChunk redirectSendWatchPacketsGetWorldChunk(ChunkHolder chunkHolder) {
        return chunkHolder.getAccessibleChunk();
    }

    @Inject(method = "makeChunkAccessible", at = @At("RETURN"))
    private void onMakeChunkAccessible(ChunkHolder chunkHolder, CallbackInfoReturnable<CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>>> cir) {
        cir.getReturnValue().thenAccept(either -> either.left().ifPresent(worldChunk -> {
            if (Config.compatibilityMode) {
                this.mainThreadExecutor.send(() -> this.sendToPlayers(worldChunk));
            } else {
                this.sendToPlayers(worldChunk);
            }
        }));
    }

    // private synthetic method_17243(Lorg/apache/commons/lang3/mutable/MutableObject;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/server/network/ServerPlayerEntity;)V
//    /**
//     * @author ishland
//     * @reason dont send chunks twice
//     */
//    @Overwrite
//    private void method_17243(MutableObject<ChunkDataS2CPacket> mutableObject, WorldChunk worldChunk, ServerPlayerEntity player) {
//        if (Config.ensureChunkCorrectness && NoTickChunkSendingInterceptor.onChunkSending(player, worldChunk.getPos().toLong()))
//            this.sendChunkDataPackets(player, mutableObject, worldChunk);
//    }

    @WrapWithCondition(method = "method_53684", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;sendToPlayers(Lnet/minecraft/world/chunk/WorldChunk;)V"))
    private boolean controlDuplicateChunkSending(ThreadedAnvilChunkStorage instance, WorldChunk worldChunk) {
        return Config.ensureChunkCorrectness;
    }

    @WrapWithCondition(method = "method_53687", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;sendToPlayers(Lnet/minecraft/world/chunk/WorldChunk;)V"))
    private boolean controlDuplicateChunkSending1(ThreadedAnvilChunkStorage instance, WorldChunk worldChunk) {
        return Config.ensureChunkCorrectness; // TODO config set to false unfixes MC-264947
    }

    // private static synthetic method_20582(Lnet/minecraft/world/chunk/Chunk;)Z
    @Dynamic
    @Inject(method = "method_20582", at = @At("RETURN"), cancellable = true) // TODO lambda expression of the 1st filter "chunk instanceof ReadOnlyChunk || chunk instanceof WorldChunk"
    private static void onSaveFilter1(Chunk chunk, CallbackInfoReturnable<Boolean> cir) {
        if (true) return;
        if (chunk instanceof WorldChunk worldChunk) {
            final ServerWorld serverWorld = (ServerWorld) worldChunk.getWorld();
            final IServerChunkManager serverChunkManager = (IServerChunkManager) serverWorld.getChunkManager();
            final IChunkTicketManager ticketManager =
                    (IChunkTicketManager) serverChunkManager.getTicketManager();
            cir.setReturnValue(cir.getReturnValueZ() && !ticketManager.getNoTickOnlyChunks().contains(chunk.getPos().toLong()));
        }
    }

}
