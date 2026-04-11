package com.ishland.c2me.rewrites.chunksystem.mixin.sync_entities;

import com.ishland.c2me.base.mixin.access.IServerWorldServerEntityHandler;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.ducks.SignallingServerEntityManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.entity.EntityHandler;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.storage.ChunkDataList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerEntityManager.class)
public abstract class MixinServerEntityManager<T extends EntityLike> implements SignallingServerEntityManager {

    @Shadow
    @Final
    private LongSet pendingUnloads;

    @Shadow
    public abstract void loadChunks();

    @Shadow
    @Final
    private EntityHandler<T> handler;
    @Unique
    private final Long2ReferenceOpenHashMap<CompletableFuture<Void>> c2me$readFutures = new Long2ReferenceOpenHashMap<>();
    @Unique
    private final Long2ReferenceOpenHashMap<CompletableFuture<Void>> c2me$unloadFutures = new Long2ReferenceOpenHashMap<>();

    @Inject(method = "scheduleRead", at = @At("RETURN"))
    private void initReadFuture(long chunkPos, CallbackInfo ci) {
        CompletableFuture<Void> old = this.c2me$readFutures.put(chunkPos, new CompletableFuture<>());
        if (old != null) {
            throw new IllegalStateException("Double read %s".formatted(new ChunkPos(chunkPos)));
        }
    }

    @ModifyExpressionValue(method = "scheduleRead", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenAccept(Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Void> forceTicks(CompletableFuture<Void> original) {
        if (this.handler instanceof IServerWorldServerEntityHandler handler1) {
            return original.thenRunAsync(this::loadChunks, ((IThreadedAnvilChunkStorage) handler1.getParentInstance().getChunkManager().chunkLoadingManager).getMainThreadExecutor());
        }
        return original;
    }

    @Inject(method = "loadChunks", at = @At(value = "INVOKE_ASSIGN", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;put(JLjava/lang/Object;)Ljava/lang/Object;"))
    private void onChunkLoad(CallbackInfo ci, @Local ChunkDataList<?> list) {
        CompletableFuture<Void> future = this.c2me$readFutures.remove(list.getChunkPos().toLong());
        if (future == null) {
            throw new IllegalStateException("Not loading %s".formatted(list.getChunkPos()));
        }
        future.complete(null);
    }

    @WrapOperation(method = "updateTrackingStatus(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/entity/EntityTrackingStatus;)V", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/LongSet;add(J)Z"))
    private boolean wrapStartUnloading(LongSet instance, long pos, Operation<Boolean> original) {
        if (this.pendingUnloads == instance) {
            CompletableFuture<Void> old = this.c2me$unloadFutures.put(pos, new CompletableFuture<>());
            if (old != null) {
                throw new IllegalStateException("Double unload %s".formatted(new ChunkPos(pos)));
            }
        }
        return original.call(instance, pos);
    }

    @WrapOperation(method = "updateTrackingStatus(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/entity/EntityTrackingStatus;)V", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/LongSet;remove(J)Z"))
    private boolean wrapCancelUnloading(LongSet instance, long pos, Operation<Boolean> original) {
        if (this.pendingUnloads == instance) {
            CompletableFuture<Void> future = this.c2me$unloadFutures.remove(pos);
            if (future != null) {
                future.cancel(false);
            }
        }
        return original.call(instance, pos);
    }

    @Inject(method = "unload(J)Z", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;remove(J)Ljava/lang/Object;"))
    private void onChunkUnload(long chunkPos, CallbackInfoReturnable<Boolean> cir) {
        CompletableFuture<Void> future = this.c2me$unloadFutures.remove(chunkPos);
        if (future == null) {
            throw new IllegalStateException("Not unloading %s".formatted(new ChunkPos(chunkPos)));
        }
        future.complete(null);
    }

    @Override
    public CompletableFuture<Void> c2me$getReadFuture(long pos) {
        return this.c2me$readFutures.get(pos);
    }

    @Override
    public CompletableFuture<Void> c2me$getUnloadFuture(long pos) {
        return this.c2me$unloadFutures.get(pos);
    }

}
