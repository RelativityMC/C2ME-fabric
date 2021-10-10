package com.ishland.c2me.mixin.notickvd;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.notickvd.IChunkHolder;
import com.ishland.c2me.mixin.access.IChunkTicketManager;
import com.ishland.c2me.mixin.access.IServerChunkManager;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage {

    @Shadow
    int watchDistance;

    @Shadow @Final private ThreadedAnvilChunkStorage.TicketManager ticketManager;

    @Shadow @Final private Long2ObjectLinkedOpenHashMap<ChunkHolder> currentChunkHolders;

    @Shadow public abstract Stream<ServerPlayerEntity> getPlayersWatchingChunk(ChunkPos chunkPos, boolean onlyOnWatchDistanceEdge);

    @Shadow
    private static int getChebyshevDistance(ChunkPos pos, ServerPlayerEntity player, boolean useWatchedPosition) {
        throw new UnsupportedOperationException();
    }

    @Shadow protected abstract void sendWatchPackets(ServerPlayerEntity player, ChunkPos pos, Packet<?>[] packets, boolean withinMaxWatchDistance, boolean withinViewDistance);

    @Shadow protected abstract void sendChunkDataPackets(ServerPlayerEntity player, Packet<?>[] packets, WorldChunk chunk);

    @Shadow @Final private ServerWorld world;

    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

    /**
     * @author ishland
     * @reason no-tick view distance: TODO replace with redirect: not sure how to redirect watchDistance without ordinal
     */
    @Overwrite
    public void setViewDistance(int watchDistance) {
        // TODO [VanillaCopy]
        int i = MathHelper.clamp(watchDistance + 1, 3, 249);
        if (i != this.watchDistance) {
            int j = this.watchDistance;
            int before = this.watchDistance; // C2ME
            this.watchDistance = Math.max(i, C2MEConfig.noTickViewDistanceConfig.viewDistance + 1); // C2ME
            ((IChunkTicketManager) this.ticketManager).invokeSetWatchDistance(i);
            this.world.getServer().getPlayerManager().sendToAll(new ChunkLoadDistanceS2CPacket(this.watchDistance));
            ObjectIterator<ChunkHolder> var4 = this.currentChunkHolders.values().iterator();

            while(var4.hasNext()) {
                ChunkHolder chunkHolder = var4.next();
                ChunkPos chunkPos = chunkHolder.getPos();
                Packet<?>[] packets = new Packet[2];
                this.getPlayersWatchingChunk(chunkPos, false).forEach((serverPlayerEntity) -> {
                    int jx = getChebyshevDistance(chunkPos, serverPlayerEntity, true);
                    boolean bl = jx <= before; // C2ME
                    boolean bl2 = jx <= this.watchDistance; // C2ME
                    this.sendWatchPackets(serverPlayerEntity, chunkPos, packets, bl, bl2);
                });
            }
        }

    }

    @Redirect(method = "sendWatchPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getWorldChunk()Lnet/minecraft/world/chunk/WorldChunk;"))
    private WorldChunk redirectSendWatchPacketsGetWorldChunk(ChunkHolder chunkHolder) {
        return ((IChunkHolder) chunkHolder).getAccessibleChunk();
    }

    @Inject(method = "makeChunkAccessible", at = @At("RETURN"))
    private void onMakeChunkAccessible(ChunkHolder chunkHolder, CallbackInfoReturnable<CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>>> cir) {
        cir.getReturnValue().thenAccept(either -> either.left().ifPresent(worldChunk -> {
            Packet<?>[] packets = new Packet[2];
            this.getPlayersWatchingChunk(worldChunk.getPos(), false).forEach((serverPlayerEntity) -> {
                if (C2MEConfig.noTickViewDistanceConfig.compatibilityMode) {
                    this.mainThreadExecutor.send(() -> this.sendChunkDataPackets(serverPlayerEntity, packets, worldChunk));
                } else {
                    this.sendChunkDataPackets(serverPlayerEntity, packets, worldChunk);
                }
            });
        }));
    }

    /**
     * move tick scheduler to ticking
     */
    @Dynamic
    @Redirect(method = "method_31416", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;disableTickSchedulers()V")) // TODO lambda expression in method_31417
    private static void redirectDisableTickScheduler(WorldChunk worldChunk) {
        // no-op
    }

    /**
     * move tick scheduler to ticking
     */
    @Dynamic
    @Inject(method = "method_18711", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;runPostProcessing()V")) // TODO lambda expression in makeChunkTickable
    private static void onMakeChunkTickable(List<WorldChunk> list, CallbackInfoReturnable<CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>>> cir) {
        final WorldChunk worldChunk = list.get(list.size() / 2);
        worldChunk.disableTickSchedulers();
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
