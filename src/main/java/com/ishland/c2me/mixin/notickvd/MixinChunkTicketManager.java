package com.ishland.c2me.mixin.notickvd;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.notickvd.IChunkTicketManager;
import com.ishland.c2me.common.notickvd.PlayerNoTickDistanceMap;
import com.ishland.c2me.mixin.access.IThreadedAnvilChunkStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
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
    private void afterTick(ThreadedAnvilChunkStorage threadedAnvilChunkStorage, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        final HashSet<ChunkPos> noTickOnlyChunksCache = new HashSet<>(this.ticketsByPosition.size() * 4);
        final ObjectIterator<Long2ObjectMap.Entry<SortedArraySet<ChunkTicket<?>>>> iterator = this.ticketsByPosition.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            final Long2ObjectMap.Entry<SortedArraySet<ChunkTicket<?>>> entry = iterator.next();
            final SortedArraySet<ChunkTicket<?>> chunkTickets = entry.getValue();
            final ChunkHolder chunkHolder = ((IThreadedAnvilChunkStorage) threadedAnvilChunkStorage).getChunkHolders().get(entry.getLongKey());
            final ChunkHolder.LevelType levelType = chunkHolder != null ? chunkHolder.getLevelType() : ChunkHolder.LevelType.ENTITY_TICKING;
            if (levelType == ChunkHolder.LevelType.BORDER && chunkTickets.size() == 1 && chunkTickets.first().getType() == PlayerNoTickDistanceMap.TICKET_TYPE) {
                noTickOnlyChunksCache.add(new ChunkPos(entry.getLongKey()));
            }
        }
        LongOpenHashSet coveredChunks = new LongOpenHashSet();
        this.ticketsByPosition.long2ObjectEntrySet().fastForEach(entry -> {
            for (ChunkTicket<?> chunkTicket : entry.getValue()) {
                if (chunkTicket.getType() == PlayerNoTickDistanceMap.TICKET_TYPE) continue;
                final int radius = 33 - chunkTicket.getLevel();
                final ChunkPos chunkPos = new ChunkPos(entry.getLongKey());
                for (int x = chunkPos.x - radius; x <= chunkPos.x + radius; x ++)
                    for (int z = chunkPos.z - radius; z <= chunkPos.z + radius; z ++) {
                        coveredChunks.add(ChunkPos.toLong(x, z));
                    }
            }
        });
        noTickOnlyChunksCache.removeIf(chunkPos -> coveredChunks.contains(chunkPos.toLong()));
        this.noTickOnlyChunksCacheView = Collections.unmodifiableSet(noTickOnlyChunksCache);
    }

    @Override
    public Set<ChunkPos> getNoTickOnlyChunks() {
        return this.noTickOnlyChunksCacheView;
    }

    @Override
    public int getNoTickPendingTicketUpdates() {
        return this.playerNoTickDistanceMap.getPendingTicketUpdatesCount();
    }
}
