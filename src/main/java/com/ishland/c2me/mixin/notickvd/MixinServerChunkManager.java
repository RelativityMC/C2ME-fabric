package com.ishland.c2me.mixin.notickvd;

import com.ishland.c2me.common.notickvd.IChunkTicketManager;
import com.ishland.c2me.common.util.FilteringIterable;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkManager.class)
public class MixinServerChunkManager {

    @Shadow @Final private ChunkTicketManager ticketManager;

//    @Dynamic
//    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getTickingFuture()Ljava/util/concurrent/CompletableFuture;")) // TODO lambda expression in tickChunks after "broadcast"
//    private static CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> redirectTickingFuture(ChunkHolder chunkHolder) {
//        return chunkHolder.getAccessibleFuture();
//    }


    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;iterateEntities()Ljava/lang/Iterable;"))
    private Iterable<Entity> redirectIterateEntities(ServerWorld serverWorld) {
        final LongSet noTickOnlyChunks = ((IChunkTicketManager) this.ticketManager).getNoTickOnlyChunks();
        if (noTickOnlyChunks == null) return serverWorld.iterateEntities();
        return new FilteringIterable<>(serverWorld.iterateEntities(), entity -> !noTickOnlyChunks.contains(entity.getChunkPos().toLong()));
    }

}
