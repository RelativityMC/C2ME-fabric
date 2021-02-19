package org.yatopiamc.c2me.mixin.threading.chunkio;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkTickScheduler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.threading.chunkio.ICachedChunkTickScheduler;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(ChunkTickScheduler.class)
public abstract class MixinChunkTickScheduler implements ICachedChunkTickScheduler {

    @Shadow public abstract ListTag toNbt();

    @Shadow @Final private ChunkPos pos;
    private AtomicReference<ListTag> preparedNbt = new AtomicReference<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        preparedNbt = new AtomicReference<>();
    }

    @Override
    public void prepareCachedNbt() {
        if (preparedNbt == null) preparedNbt = new AtomicReference<>();
        preparedNbt.set(toNbt());
    }

    @Override
    public ListTag getCachedNbt() {
        if (preparedNbt == null) preparedNbt = new AtomicReference<>();
        if (preparedNbt.get() == null) {
            new IllegalStateException("Tried to serialize ticklist with no cached nbt for chunk " + pos + "! This will affect data integrity. Incompatible mods?").printStackTrace();
            prepareCachedNbt();
        }
        final ListTag preparedNbt = this.preparedNbt.get();
        this.preparedNbt = null;
        return preparedNbt;
    }
}
