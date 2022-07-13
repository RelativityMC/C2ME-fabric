package com.ishland.c2me.opts.chunkio.mixin.async_chunk_on_player_login;

import com.ishland.c2me.base.mixin.access.IServerChunkManager;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.opts.chunkio.common.async_chunk_on_player_login.IAsyncChunkPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.function.Function;

@Mixin(PlayerManager.class)
public abstract class MixinPlayerManager {

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract void sendWorldInfo(ServerPlayerEntity player, ServerWorld world);

    private static final ChunkTicketType<Unit> ASYNC_PLAYER_LOGIN = ChunkTicketType.create("async_player_login", (unit, unit2) -> 0);

    @Redirect(
            method = "onPlayerConnect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;requestTeleport(DDDFF)V")
    )
    private void redirectRequestTeleport(ServerPlayNetworkHandler instance, double x, double y, double z, float yaw, float pitch) {
        final ServerChunkManager chunkManager = ((ServerWorld) instance.player.world).getChunkManager();
        final ChunkTicketManager ticketManager = ((IServerChunkManager) chunkManager).getTicketManager();

        final ChunkPos pos = new ChunkPos(new BlockPos(x, y, z));
        ticketManager.addTicket(ASYNC_PLAYER_LOGIN, pos, 2, Unit.INSTANCE);
        ((IServerChunkManager) chunkManager).invokeTick();
        final ChunkHolder chunkHolder = ((IThreadedAnvilChunkStorage) chunkManager.threadedAnvilChunkStorage).getCurrentChunkHolders().get(pos.toLong());
        if (chunkHolder == null) {
            throw new IllegalStateException("Chunk not there when requested");
        }
        instance.player.notInAnyWorld = true; // suppress move packets

        chunkHolder.getEntityTickingFuture().whenCompleteAsync((worldChunkUnloadedEither, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Error while loading chunks", throwable);
                return;
            }
            if (!instance.connection.isOpen()) {
                return;
            }

            instance.player.notInAnyWorld = false;
            instance.requestTeleport(x, y, z, yaw, pitch);
            ((ServerWorld) instance.player.world).onPlayerConnected(instance.player);
            this.sendWorldInfo(instance.player, (ServerWorld) instance.player.world);
            instance.player.onSpawn();

            final NbtCompound playerData = ((IAsyncChunkPlayer) instance.player).getPlayerData();
            c2me$mountSavedVehicles(instance.player, playerData);

            LOGGER.info("Async chunk loading for player {} completed", instance.player.getName().getString());
        }, ((IServerChunkManager) chunkManager).getMainThreadExecutor());
    }

    private void c2me$mountSavedVehicles(ServerPlayerEntity player, NbtCompound playerData) {
        if (playerData != null && playerData.contains("RootVehicle", NbtElement.COMPOUND_TYPE)) {
            NbtCompound nbtCompound2 = playerData.getCompound("RootVehicle");
            ServerWorld world = (ServerWorld) player.world;
            Entity entity = EntityType.loadEntityWithPassengers(
                    nbtCompound2.getCompound("Entity"), world, vehicle -> !world.tryLoadEntity(vehicle) ? null : vehicle
            );
            if (entity != null) {
                UUID uUID;
                if (nbtCompound2.containsUuid("Attach")) {
                    uUID = nbtCompound2.getUuid("Attach");
                } else {
                    uUID = null;
                }

                if (entity.getUuid().equals(uUID)) {
                    player.startRiding(entity, true);
                } else {
                    for(Entity entity2 : entity.getPassengersDeep()) {
                        if (entity2.getUuid().equals(uUID)) {
                            player.startRiding(entity2, true);
                            break;
                        }
                    }
                }

                if (!player.hasVehicle()) {
                    LOGGER.warn("Couldn't reattach entity to player");
                    entity.discard();

                    for(Entity entity2 : entity.getPassengersDeep()) {
                        entity2.discard();
                    }
                }
            }
        }
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onPlayerConnected(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void delayAddToWorld(ServerWorld instance, ServerPlayerEntity player) {
        // no-op
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendWorldInfo(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/world/ServerWorld;)V"))
    private void delaySendWorldInfo(PlayerManager instance, ServerPlayerEntity player, ServerWorld world) {
        // no-op
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;onSpawn()V"))
    private void delayPlayerSpawn(ServerPlayerEntity instance) {
        // no-op
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;loadEntityWithPassengers(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/world/World;Ljava/util/function/Function;)Lnet/minecraft/entity/Entity;"))
    private @Nullable Entity delayPassengerMount(NbtCompound nbt, World world, Function<Entity, Entity> entityProcessor) {
        return null; // no-op
    }

    @Inject(method = "loadPlayerData", at = @At(value = "RETURN"))
    private void onLoadPlayerData(ServerPlayerEntity player, CallbackInfoReturnable<NbtCompound> cir) {
        ((IAsyncChunkPlayer) player).setPlayerData(cir.getReturnValue());
    }

}
