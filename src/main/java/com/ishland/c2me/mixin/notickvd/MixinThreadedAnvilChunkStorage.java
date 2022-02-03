package com.ishland.c2me.mixin.notickvd;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.notickvd.IChunkHolder;
import com.ishland.c2me.mixin.access.IServerChunkManager;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
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

    @Shadow protected abstract void sendChunkDataPackets(ServerPlayerEntity player, MutableObject<ChunkDataS2CPacket> mutableObject, WorldChunk chunk);

    @Shadow public abstract List<ServerPlayerEntity> getPlayersWatchingChunk(ChunkPos chunkPos, boolean onlyOnWatchDistanceEdge);

    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

    @ModifyArg(method = "setViewDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I"), index = 2)
    private int modifyMaxVD(int max) {
        return 251;
    }

    @Redirect(method = "sendWatchPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getWorldChunk()Lnet/minecraft/world/chunk/WorldChunk;"))
    private WorldChunk redirectSendWatchPacketsGetWorldChunk(ChunkHolder chunkHolder) {
        return ((IChunkHolder) chunkHolder).getAccessibleChunk();
    }

    @Inject(method = "method_31417", at = @At("RETURN"))
    private void onMakeChunkAccessible(ChunkHolder chunkHolder, CallbackInfoReturnable<CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>>> cir) {
        cir.getReturnValue().thenAccept(either -> either.left().ifPresent(worldChunk -> {
            MutableObject<ChunkDataS2CPacket> mutableObject = new MutableObject<>();
            this.getPlayersWatchingChunk(worldChunk.getPos(), false).forEach((serverPlayerEntity) -> {
                if (C2MEConfig.noTickViewDistanceConfig.compatibilityMode) {
                    this.mainThreadExecutor.send(() -> this.sendChunkDataPackets(serverPlayerEntity, mutableObject, worldChunk));
                } else {
                    this.sendChunkDataPackets(serverPlayerEntity, mutableObject, worldChunk);
                }
            });
        }));
    }

    // private synthetic method_17243(Lorg/apache/commons/lang3/mutable/MutableObject;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/server/network/ServerPlayerEntity;)V
    /**
     * @author ishland
     * @reason dont send chunks twice
     */
    @Overwrite
    private void method_17243(MutableObject<ChunkDataS2CPacket> mutableObject, WorldChunk worldChunk, ServerPlayerEntity player) {
        if (C2MEConfig.noTickViewDistanceConfig.ensureChunkCorrectness)
            this.sendChunkDataPackets(player, mutableObject, worldChunk);
    }

    // private static synthetic method_20582(Lnet/minecraft/world/chunk/Chunk;)Z
    @Dynamic
    @Inject(method = "method_20582", at = @At("RETURN"), cancellable = true) // TODO lambda expression of the 1st filter "chunk instanceof ReadOnlyChunk || chunk instanceof WorldChunk"
    private static void onSaveFilter1(Chunk chunk, CallbackInfoReturnable<Boolean> cir) {
        if (true) return;
        if (chunk instanceof WorldChunk worldChunk) {
            final ServerWorld serverWorld = (ServerWorld) worldChunk.getWorld();
            final IServerChunkManager serverChunkManager = (IServerChunkManager) serverWorld.getChunkManager();
            final com.ishland.c2me.common.notickvd.IChunkTicketManager ticketManager =
                    (com.ishland.c2me.common.notickvd.IChunkTicketManager) serverChunkManager.getTicketManager();
            cir.setReturnValue(cir.getReturnValueZ() && !ticketManager.getNoTickOnlyChunks().contains(chunk.getPos().toLong()));
        }
    }

}
