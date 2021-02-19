package org.yatopiamc.c2me.mixin.threading.chunkio;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.threading.chunkio.ICachedServerTickScheduler;

import java.util.concurrent.ConcurrentHashMap;

@Mixin(ServerTickScheduler.class)
public abstract class MixinServerTickScheduler implements ICachedServerTickScheduler {

    @Shadow public abstract ListTag toNbt(ChunkPos chunkPos);

    private ConcurrentHashMap<ChunkPos, ListTag> cachedNbt = new ConcurrentHashMap<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        cachedNbt = new ConcurrentHashMap<>();
    }

    @Override
    public void prepareCachedNbt(ChunkPos pos) {
        Preconditions.checkNotNull(pos);
        if (cachedNbt == null) cachedNbt = new ConcurrentHashMap<>();
        cachedNbt.put(pos, toNbt(pos));
    }

    @Override
    public ListTag getCachedNbt(ChunkPos pos) {
        Preconditions.checkNotNull(pos);
        if (cachedNbt == null) cachedNbt = new ConcurrentHashMap<>();
        if (!cachedNbt.containsKey(pos)) {
            new IllegalStateException("Tried to serialize ticklist with no cached nbt for chunk " + pos + "! This will affect data integrity. Incompatible mods?").printStackTrace();
            prepareCachedNbt(pos);
        }
        return this.cachedNbt.remove(pos);
    }
}
