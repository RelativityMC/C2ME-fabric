package com.ishland.c2me.mixin.notickvd;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.notickvd.IChunkTicketManager;
import com.ishland.c2me.common.notickvd.NoTickSystem;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkTicketManager.class)
public class MixinChunkTicketManager implements IChunkTicketManager {

    @Shadow private long age;
    @Mutable
    @Shadow @Final private ChunkTicketManager.NearbyChunkTicketUpdater nearbyChunkTicketUpdater;

    @Unique
    private NoTickSystem noTickSystem;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.noTickSystem = new NoTickSystem((ChunkTicketManager) (Object) this);
    }

    @Inject(method = "handleChunkEnter", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkTicketManager$DistanceFromNearestPlayerTracker;updateLevel(JIZ)V", ordinal = 0, shift = At.Shift.AFTER))
    private void onHandleChunkEnter(ChunkSectionPos pos, ServerPlayerEntity player, CallbackInfo ci) {
        this.noTickSystem.addPlayerSource(pos.toChunkPos());
    }

    @Inject(method = "handleChunkLeave", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkTicketManager$DistanceFromNearestPlayerTracker;updateLevel(JIZ)V", ordinal = 0, shift = At.Shift.AFTER))
    private void onHandleChunkLeave(ChunkSectionPos pos, ServerPlayerEntity player, CallbackInfo ci) {
        this.noTickSystem.removePlayerSource(pos.toChunkPos());
    }

    @Inject(method = "purge", at = @At("RETURN"))
    private void onPurge(CallbackInfo ci) {
        this.noTickSystem.runPurge(this.age);
        this.noTickSystem.tick();
    }

    @Inject(method = "addTicket(JLnet/minecraft/server/world/ChunkTicket;)V", at = @At("RETURN"))
    private void onAddTicket(long position, ChunkTicket<?> ticket, CallbackInfo ci) {
//        if (ticket.getType() != ChunkTicketType.UNKNOWN) System.err.printf("Added ticket (%s) at %s\n", ticket, new ChunkPos(position));
        this.noTickSystem.onTicketAdded(position, ticket);
    }

    @Inject(method = "removeTicket(JLnet/minecraft/server/world/ChunkTicket;)V", at = @At("RETURN"))
    private void onRemoveTicket(long pos, ChunkTicket<?> ticket, CallbackInfo ci) {
//        if (ticket.getType() != ChunkTicketType.UNKNOWN) System.err.printf("Removed ticket (%s) at %s\n", ticket, new ChunkPos(pos));
        this.noTickSystem.onTicketRemoved(pos, ticket);
    }

    @Inject(method = "setWatchDistance", at = @At("RETURN"))
    public void onSetWatchDistance(int viewDistance, CallbackInfo ci) {
        this.noTickSystem.setNoTickViewDistance(Math.max(viewDistance, C2MEConfig.noTickViewDistanceConfig.viewDistance + 1)); // TODO not final
    }

    @ModifyArg(method = "setWatchDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkTicketManager$NearbyChunkTicketUpdater;setWatchDistance(I)V"))
    private int modifyNormalVD(int viewDistance) {
        return MathHelper.clamp(viewDistance, 3, 33);
    }

    @Override
    @Unique
    public LongSet getNoTickOnlyChunks() {
        return this.noTickSystem.getNoTickOnlyChunksSnapshot();
    }

    @Override
    @Unique
    public int getNoTickPendingTicketUpdates() {
        return this.noTickSystem.getPendingNoTickTicketUpdatesCount();
    }
}
