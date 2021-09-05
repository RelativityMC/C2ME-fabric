package com.ishland.c2me.mixin.notickvd;

import com.google.common.base.Suppliers;
import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.notickvd.IChunkTicketManager;
import com.ishland.c2me.common.notickvd.NormalTicketDistanceMap;
import com.ishland.c2me.common.notickvd.PlayerNoTickDistanceMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.collection.SortedArraySet;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(ChunkTicketManager.class)
public class MixinChunkTicketManager implements IChunkTicketManager {

    @Shadow
    @Final
    private Long2ObjectOpenHashMap<SortedArraySet<ChunkTicket<?>>> ticketsByPosition;
    @Shadow private long age;
    private PlayerNoTickDistanceMap playerNoTickDistanceMap;
    private NormalTicketDistanceMap normalTicketDistanceMap;

    private volatile Supplier<LongSet> noTickOnlyChunksCacheView;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        playerNoTickDistanceMap = new PlayerNoTickDistanceMap((ChunkTicketManager) (Object) this, C2MEConfig.noTickViewDistanceConfig.viewDistance + 1);
        normalTicketDistanceMap = new NormalTicketDistanceMap((ChunkTicketManager) (Object) this);
    }

    @Inject(method = "handleChunkEnter", at = @At("HEAD"))
    private void onHandleChunkEnter(ChunkSectionPos pos, ServerPlayerEntity player, CallbackInfo ci) {
        playerNoTickDistanceMap.addSource(pos.toChunkPos());
    }

    @Inject(method = "handleChunkLeave", at = @At("HEAD"))
    private void onHandleChunkLeave(ChunkSectionPos pos, ServerPlayerEntity player, CallbackInfo ci) {
        playerNoTickDistanceMap.removeSource(pos.toChunkPos());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(ThreadedAnvilChunkStorage threadedAnvilChunkStorage, CallbackInfoReturnable<Boolean> info) {
        this.playerNoTickDistanceMap.update(threadedAnvilChunkStorage);
        if (this.normalTicketDistanceMap.update(threadedAnvilChunkStorage)) {
            this.noTickOnlyChunksCacheView = Suppliers.memoize(() -> {
                final LongSet noTickChunks = this.playerNoTickDistanceMap.getChunks();
                final LongSet normalChunks = this.normalTicketDistanceMap.getChunks();
                final LongOpenHashSet longs = new LongOpenHashSet(noTickChunks.size() * 3 / 2);
                final LongIterator iterator = noTickChunks.iterator();
                while (iterator.hasNext()) {
                    final long chunk = iterator.nextLong();
                    if (normalChunks.contains(chunk)) continue;
                    longs.add(chunk);
                }
                return LongSets.unmodifiable(longs);
            });
        }
    }

    @Inject(method = "purge", at = @At("RETURN"))
    private void onPurge(CallbackInfo ci) {
        this.normalTicketDistanceMap.purge(this.age);
    }

    @Inject(method = "addTicket(JLnet/minecraft/server/world/ChunkTicket;)V", at = @At("RETURN"))
    private void onAddTicket(long position, ChunkTicket<?> ticket, CallbackInfo ci) {
//        if (ticket.getType() != ChunkTicketType.UNKNOWN) System.err.printf("Added ticket (%s) at %s\n", ticket, new ChunkPos(position));
        this.normalTicketDistanceMap.addTicket(position, ticket);
    }

    @Inject(method = "removeTicket(JLnet/minecraft/server/world/ChunkTicket;)V", at = @At("RETURN"))
    private void onRemoveTicket(long pos, ChunkTicket<?> ticket, CallbackInfo ci) {
//        if (ticket.getType() != ChunkTicketType.UNKNOWN) System.err.printf("Removed ticket (%s) at %s\n", ticket, new ChunkPos(pos));
        this.normalTicketDistanceMap.removeTicket(pos, ticket);
    }

    @Override
    @Unique
    public LongSet getNoTickOnlyChunks() {
        return this.noTickOnlyChunksCacheView != null ? this.noTickOnlyChunksCacheView.get() : null;
    }

    @Override
    @Unique
    public int getNoTickPendingTicketUpdates() {
        return this.playerNoTickDistanceMap.getPendingTicketUpdatesCount();
    }
}
