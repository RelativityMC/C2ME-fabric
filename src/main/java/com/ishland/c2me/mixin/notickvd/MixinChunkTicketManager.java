package com.ishland.c2me.mixin.notickvd;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.notickvd.IChunkTicketManager;
import com.ishland.c2me.common.notickvd.PlayerNoTickDistanceMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.collection.SortedArraySet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Mixin(ChunkTicketManager.class)
public class MixinChunkTicketManager implements IChunkTicketManager {

    @Shadow
    @Final
    private Long2ObjectOpenHashMap<SortedArraySet<ChunkTicket<?>>> ticketsByPosition;
    private PlayerNoTickDistanceMap playerNoTickDistanceMap;

    private volatile Set<ChunkPos> noTickOnlyChunksCacheView;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        playerNoTickDistanceMap = new PlayerNoTickDistanceMap((ChunkTicketManager) (Object) this, C2MEConfig.noTickViewDistanceConfig.viewDistance + 1);
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
        playerNoTickDistanceMap.update(threadedAnvilChunkStorage);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void afterTick(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        final HashSet<ChunkPos> noTickOnlyChunksCache = new HashSet<>(this.ticketsByPosition.size() * 4);
        final ObjectIterator<Long2ObjectMap.Entry<SortedArraySet<ChunkTicket<?>>>> iterator = this.ticketsByPosition.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            final Long2ObjectMap.Entry<SortedArraySet<ChunkTicket<?>>> entry = iterator.next();
            final SortedArraySet<ChunkTicket<?>> chunkTickets = entry.getValue();
            if (chunkTickets.size() == 1 && chunkTickets.first().getType() == PlayerNoTickDistanceMap.TICKET_TYPE) {
                noTickOnlyChunksCache.add(new ChunkPos(entry.getLongKey()));
            }
        }
        this.noTickOnlyChunksCacheView = Collections.unmodifiableSet(noTickOnlyChunksCache);
    }

    @Override
    public Set<ChunkPos> getNoTickOnlyChunks() {
        return this.noTickOnlyChunksCacheView;
    }
}
