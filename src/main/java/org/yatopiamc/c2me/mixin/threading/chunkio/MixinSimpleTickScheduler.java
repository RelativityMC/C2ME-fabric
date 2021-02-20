package org.yatopiamc.c2me.mixin.threading.chunkio;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.world.SimpleTickScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.threading.chunkio.ICachedChunkTickScheduler;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(SimpleTickScheduler.class)
public abstract class MixinSimpleTickScheduler implements ICachedChunkTickScheduler {

    @Shadow
    public abstract ListTag toNbt();

    private AtomicReference<Optional<ListTag>> preparedNbt = new AtomicReference<>();
    private AtomicReference<Executor> fallbackExecutor = new AtomicReference<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        preparedNbt = new AtomicReference<>();
        fallbackExecutor = new AtomicReference<>();
    }

    @Override
    public void setFallbackExecutor(Executor executor) {
        fallbackExecutor.set(executor);
    }

    @Override
    public void prepareCachedNbt() {
        if (preparedNbt == null) preparedNbt = new AtomicReference<>();
        preparedNbt.set(Optional.ofNullable(toNbt()));
    }

    @Override
    public ListTag getCachedNbt() {
        if (preparedNbt == null) preparedNbt = new AtomicReference<>();
        //noinspection OptionalAssignedToNull
        if (preparedNbt.get() == null) {
            System.err.println("Tried to serialize ticklist with no cached nbt! This will affect performance. Incompatible mods?");
            if (fallbackExecutor.get() != null)
                return CompletableFuture.supplyAsync(this::toNbt, fallbackExecutor.get()).join();
            else throw new RuntimeException("no fallback executor found");
        }
        final ListTag preparedNbt = this.preparedNbt.get().orElse(null);
        this.preparedNbt.set(null);
        return preparedNbt;
    }
}
